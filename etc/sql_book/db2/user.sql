--授权用户访问数据库权限
GRANT BINDADD ON DATABASE TO USER dstuser;
GRANT CONNECT ON DATABASE TO USER dstuser;
GRANT LOAD ON DATABASE TO USER dstuser;

--授予用户访问表空间的权限
GRANT USE OF TABLESPACE GD_MAIN_TBS TO USER dstuser;

--授予用户操作模式的权限
GRANT ALTERIN ON SCHEMA dstuser TO USER dstuser;
GRANT CREATEIN ON SCHEMA dstuser TO USER dstuser;
GRANT DROPIN ON SCHEMA dstuser TO USER dstuser;

--授予用户读取表权限
GRANT SELECT ON TABLE ECGD.CL_COMMODITY TO USER dstuser;