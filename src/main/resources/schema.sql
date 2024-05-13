-- 创建一些建表时会用到的枚举类型, 枚举类型只能是字符串
drop type if exists sex;
CREATE TYPE sex AS ENUM ('male', 'female');

drop type if exists status;
CREATE TYPE status AS ENUM ('0', '1');

drop type if exists "type";
CREATE TYPE "type" AS ENUM ('1', '2');

-- 创建隐式转换函数
CREATE CAST (varchar AS sex) WITH INOUT AS IMPLICIT;
create cast (integer as status) with inout as implicit;
create cast (integer as "type") with inout as implicit;

create cast (status as integer) with inout as implicit;
create cast ("type" as integer) with inout as implicit;

-- 每个表中基本都包含着创建人, 更新人, 这些字段可以添加外键约束.
-- 但是为了修改起来方便, 这里也没有添加

DROP TABLE IF EXISTS employee;
CREATE TABLE employee
(
    id          bigserial PRIMARY KEY NOT NULL,
    name        VARCHAR(32)           NOT NULL,
    username    VARCHAR(32)           NOT NULL UNIQUE,
    password    VARCHAR(64)           NOT NULL,
    phone       VARCHAR(11)           NOT NULL,
    sex         sex                   NOT NULL,
    id_number   VARCHAR(18)           NOT NULL,
    status      status                NOT NULL DEFAULT '1',
    create_time TIMESTAMP             NOT NULL,
    update_time TIMESTAMP             NOT NULL,
    create_user bigint                NOT NULL,
    update_user bigint                NOT NULL
    -- 不需要额外声明 UNIQUE INDEX，因为 username 列已经定义了 UNIQUE 约束
);
-- PostgreSQL 中注释不能在建表时添加, 多有不便
comment on table employee is '员工信息';
COMMENT ON COLUMN employee.id IS '主键';
comment on column employee.id_number is '身份证号';
COMMENT ON COLUMN employee.status IS '状态; 1: 启用, 0: 禁用';

-- 菜品分类
DROP TABLE IF EXISTS category;
CREATE TABLE category
(
    id          bigserial   NOT NULL primary key,
    type        "type"      NOT NULL,
    name        VARCHAR(64) NOT NULL unique,
    sort        int         NOT NULL default 0,
    create_time TIMESTAMP   NOT NULL,
    update_time TIMESTAMP   NOT NULL,
    create_user bigint      NOT NULL,
    update_user bigint      NOT NULL
);
comment on table category is '菜品及套餐分类';
comment on column category.type is '类型; 1: 菜品分类, 2: 套餐分类';
comment on column category.name is '分类名称, 默认唯一';
comment on column category.sort is '排序字段';

-- 菜品管理
drop table if exists dish;
create table dish
(
    id          bigserial      not null primary key,
    name        varchar(64)    not null unique,
    category_id bigint         not null,
    -- 或许我可以为其添加外键约束?
    -- 菜品其对应的分类只能是 **菜品分类**! 而不能是套餐分类, 或许后面也可以为这个地方增加约束
    -- 所以刚才考虑的 "如果多个套餐引用相同的菜品" 这个问题纯属多虑.
    -- 而且, 菜品分类都是类似于 "川菜", "粤菜", "主食" 等, 一般可认为一个菜品只能属于其中分类的一种.
    -- 所以不必使用数组类型进行存储
    price       decimal(10, 2) not null,
    code        varchar(64)    not null,
    image       varchar(255)   not null,
    description varchar(512)   not null,
    status      status         not null default '1',
    sort        int            not null default 0,
    deleted     boolean        not null default false,
    create_time TIMESTAMP      NOT NULL,
    update_time TIMESTAMP      NOT NULL,
    create_user bigint         NOT NULL,
    update_user bigint         NOT NULL
);
comment on table dish is '菜品管理';
comment on column dish.category_id is '菜品分类 id';
comment on column dish.code is '商品码'; -- 有什么用吗?
comment on column dish.image is '图片文件名';
comment on column dish.description is '描述信息';
comment on column dish.deleted is '是否删除, 逻辑删除';


drop table if exists dish_flavor;
create table dish_flavor
(
    id          bigserial    not null primary key,
    dish_id     bigint       not null, -- 也许我可以为其添加外键?
    name        varchar(64)  not null,
    value       varchar(8)[] not null, -- 这里使用了 pgsql 提供的数组类型
    deleted     boolean      not null default false,
    create_time TIMESTAMP    NOT NULL,
    update_time TIMESTAMP    NOT NULL,
    create_user bigint       NOT NULL,
    update_user bigint       NOT NULL
);
comment on table dish_flavor is '菜品口味关系表';
comment on column dish_flavor.dish_id is '菜品id'; -- 每一种菜品对应着多种口味, 口味又可以取不同的值
comment on column dish_flavor.name is '口味名称';
comment on column dish_flavor.value is '口味值'; -- 口味值采用数组存储
comment on column dish_flavor.deleted is '是否删除, 逻辑删除';


drop table if exists set_meal;
create table set_meal
(
    id          bigserial      not null primary key,
    category_id bigint         not null,
    name        varchar(64)    not null unique,
    -- 或许我可以为其添加外键约束?
    -- 套餐其对应的分类只能是 **套餐分类**! 而不能是菜品分类, 或许后面也可以为这个地方增加约束
    price       decimal(10, 2) not null,
    code        varchar(64)    not null,
    image       varchar(255)   not null,
    description varchar(512)   not null,
    status      status         not null default '1',
    sort        int            not null default 0,
    deleted     boolean        not null default false,
    create_time TIMESTAMP      NOT NULL,
    update_time TIMESTAMP      NOT NULL,
    create_user bigint         NOT NULL,
    update_user bigint         NOT NULL
);
comment on table set_meal is '套餐';
comment on column set_meal.category_id is '套餐分类 id';
comment on column set_meal.code is '商品码'; -- 有什么用吗?
comment on column set_meal.image is '图片文件名';
comment on column set_meal.description is '描述信息';
comment on column set_meal.deleted is '是否删除, 逻辑删除';

drop table if exists set_meal_dish;
create table set_meal_dish
(
    id          bigserial      not null primary key,
    set_meal_id bigint         not null, -- 或许我可以为其添加外键约束?
    dish_id     bigint         not null, -- 或许我可以为其添加外键约束?
    name        varchar(64)    not null,
    price       decimal(10, 2) not null,
    copies      int            not null,
    sort        int            not null default 0,
    deleted     boolean        not null default false,
    create_time TIMESTAMP      NOT NULL,
    update_time TIMESTAMP      NOT NULL,
    create_user bigint         NOT NULL,
    update_user bigint         NOT NULL
);
comment on table set_meal_dish is '套餐与菜品关系';
comment on column set_meal_dish.set_meal_id is '套餐 id';
comment on column set_meal_dish.dish_id is '菜品 id';
comment on column set_meal_dish.name is '菜品名称 (冗余字段)';
comment on column set_meal_dish.price is '菜品原件 (冗余字段)';
comment on column set_meal_dish.copies is '份数';
comment on column set_meal_dish.deleted is '是否删除, 逻辑删除';