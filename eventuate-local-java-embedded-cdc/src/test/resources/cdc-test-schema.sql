/**
 * Created by cer on 6/21/17.
 */

drop database if exists `eventuate-database-with-dash`;
create database `eventuate-database-with-dash`;
drop table if exists `eventuate-database-with-dash`.`foobar`;
create table `eventuate-database-with-dash`.`foobar` (bar tinyint primary key);
drop table if exists `eventuate`.`eventuate-test-table-with-dash`;
create table `eventuate`.`eventuate-test-table-with-dash` (bar tinyint primary key);
