--当前时间
select now();
--当前时间
select current_date;
--生成测试表海量插入数据
create table tbl_test (id int, info text, c_time timestamp);
insert into tbl_test select generate_series(1,10000000),md5(random()::text),clock_timestamp();

--查看触发器
select * from information_schema.triggers;

--查看数据库连接
SELECT hh_stat_get_backend_pid(s.backendid) AS procpid,
       hh_stat_get_backend_activity(s.backendid) AS current_query
    FROM (SELECT hh_stat_get_backend_idset() AS backendid) AS s;
   
--查看正在运行的sql
SELECT
    procpid,
    start,
    now() - start AS lap,
    current_query
FROM
    (SELECT
        backendid,
        hh_stat_get_backend_pid(S.backendid) AS procpid,
        hh_stat_get_backend_activity_start(S.backendid) AS start,
        hh_stat_get_backend_activity(S.backendid) AS current_query
    FROM
        (SELECT hh_stat_get_backend_idset() AS backendid) AS S
    ) AS S
WHERE
   current_query <> '<IDLE>'
ORDER BY
   lap DESC;


