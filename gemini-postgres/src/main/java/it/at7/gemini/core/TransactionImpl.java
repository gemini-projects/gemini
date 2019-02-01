package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiGenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class TransactionImpl implements Transaction {
    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);
    private Connection connection;
    private boolean committed;

    private final DataSource dataSource;

    @Autowired
    public TransactionImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void open() throws GeminiGenericException {
        try {
            this.connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public void close() throws GeminiException {
        try {
            if (!committed) {
                rollback();
            }
            this.connection.close();
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    public void commit() throws GeminiException {
        try {
            this.connection.commit();
            this.committed = true;
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    public void rollback() throws GeminiException {
        try {
            this.connection.rollback();
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        return executeUpdate(sql, null);
    }

    public int executeUpdate(String sql, @Nullable Map<String, Object> parameters) throws SQLException {
        try (PreparedStatement ps = getPreparedStatement(sql, parameters)) {
            return ps.executeUpdate();
        }
    }

    public long executeInsert(String sql, @Nullable Map<String, Object> parameters) throws SQLException {
        try (PreparedStatement ps = getPreparedStatement(sql, parameters)) {
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public <R> R executeQuery(String sql, @Nullable Map<String, Object> parameters, CallbackWithResultThrowingSqlException<R, ResultSet> callback) throws SQLException, GeminiException {
        try (PreparedStatement ps = getPreparedStatement(sql, parameters)) {
            ResultSet resultSet = ps.executeQuery();
            return callback.accept(resultSet);
        }
    }

    public void executeQuery(String sql, CallbackThrowingSqlException<ResultSet> callback) throws SQLException {
        executeQuery(sql, null, callback);
    }

    public void executeQuery(String sql, @Nullable Map<String, Object> parameters, CallbackThrowingSqlException<ResultSet> callback) throws SQLException {
        try (PreparedStatement ps = getPreparedStatement(sql, parameters)) {
            ResultSet resultSet = ps.executeQuery();
            callback.accept(resultSet);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, @Nullable Map<String, Object> parameters) throws SQLException {
        SqlParameterSource paramSource = new MapSqlParameterSource(parameters);
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        PreparedStatementCreatorFactory psCreatorFactory = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
        psCreatorFactory.setReturnGeneratedKeys(true);
        PreparedStatementCreator psCreator = psCreatorFactory.newPreparedStatementCreator(params);
        PreparedStatement preparedStatement = psCreator.createPreparedStatement(connection);
        logger.debug(preparedStatement.toString());
        /* logger.debug(
                preparedStatement.unwrap(com.p.jdbc.JDBC42PreparedStatement.class).asSql()
        ); */
        return preparedStatement;
    }

    @FunctionalInterface
    public interface CallbackWithResultThrowingSqlException<R, T> {
        R accept(T t) throws SQLException, GeminiException;
    }

    @FunctionalInterface
    public interface CallbackThrowingSqlException<T> {
        void accept(T t) throws SQLException;
    }

    /*
    enum Autocommit {
        AUTOCOMMIT_FALSE(false),
        AUTOCOMMIT_TRUE(true);

        private final boolean b;

        Autocommit(boolean b) {
            this.b = b;
        }

        public boolean getValue() {
            return b;
        }
    }*/
}
