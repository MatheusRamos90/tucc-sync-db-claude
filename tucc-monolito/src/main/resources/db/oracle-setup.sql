-- =============================================================================
-- oracle-setup.sql
-- Executado como SYSTEM no XEPDB1 pelo container gvenzl/oracle-xe
-- Caminho no container: /container-entrypoint-initdb.d/setup/01-setup.sql
-- =============================================================================

-- Garante privilégios necessários ao usuário tucc para Debezium (LogMiner)
GRANT CREATE SESSION         TO tucc;
GRANT CREATE TABLE           TO tucc;
GRANT CREATE SEQUENCE        TO tucc;
GRANT SELECT_CATALOG_ROLE    TO tucc;
GRANT EXECUTE_CATALOG_ROLE   TO tucc;
GRANT SELECT ANY TRANSACTION TO tucc;
GRANT FLASHBACK ANY TABLE    TO tucc;
GRANT SELECT ANY TABLE       TO tucc;
GRANT LOCK ANY TABLE         TO tucc;
GRANT EXECUTE ON SYS.DBMS_LOGMNR TO tucc;
GRANT SELECT ON SYS.V_$LOG             TO tucc;
GRANT SELECT ON SYS.V_$LOG_HISTORY     TO tucc;
GRANT SELECT ON SYS.V_$LOGMNR_LOGS     TO tucc;
GRANT SELECT ON SYS.V_$LOGMNR_CONTENTS TO tucc;
GRANT SELECT ON SYS.V_$LOGFILE         TO tucc;
GRANT SELECT ON SYS.V_$ARCHIVED_LOG    TO tucc;
GRANT SELECT ON SYS.V_$SEQUENCE        TO tucc;
GRANT SELECT ON SYS.V_$PARAMETER       TO tucc;
GRANT LOGMINING TO tucc;
