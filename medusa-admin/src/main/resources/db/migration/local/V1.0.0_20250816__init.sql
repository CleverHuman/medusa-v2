CREATE TABLE `gen_table`
(
    `table_id`          bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `table_name`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '表名称',
    `table_comment`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '表描述',
    `sub_table_name`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '关联子表的表名',
    `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '子表关联的外键名',
    `class_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '实体类名称',
    `tpl_category`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
    `tpl_web_type`      varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
    `package_name`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '生成包路径',
    `module_name`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '生成模块名',
    `business_name`     varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '生成业务名',
    `function_name`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '生成功能名',
    `function_author`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '生成功能作者',
    `gen_type`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci       DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
    `gen_path`          varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
    `options`           varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它生成选项',
    `create_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '创建者',
    `create_time`       datetime                                                       DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '更新者',
    `update_time`       datetime                                                       DEFAULT NULL COMMENT '更新时间',
    `remark`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`table_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='代码生成业务表';


CREATE TABLE `gen_table_column`
(
    `column_id`      bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `table_id`       bigint                                                        DEFAULT NULL COMMENT '归属表编号',
    `column_name`    varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列名称',
    `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列描述',
    `column_type`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列类型',
    `java_type`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA类型',
    `java_field`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
    `is_pk`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否主键（1是）',
    `is_increment`   char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否自增（1是）',
    `is_required`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否必填（1是）',
    `is_insert`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否为插入字段（1是）',
    `is_edit`        char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否编辑字段（1是）',
    `is_list`        char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否列表字段（1是）',
    `is_query`       char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT NULL COMMENT '是否查询字段（1是）',
    `query_type`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
    `html_type`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    `dict_type`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
    `sort`           int                                                           DEFAULT NULL COMMENT '排序',
    `create_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`    datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`    datetime                                                      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`column_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='代码生成业务表字段';


CREATE TABLE `mall_ak_sk`
(
    `id`         bigint                                                        NOT NULL AUTO_INCREMENT,
    `access_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `secret_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `status`     tinyint DEFAULT '1' COMMENT '1= 0=',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_access_key` (`access_key`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_ak_sk';

/*!40000 ALTER TABLE `mall_ak_sk`
    DISABLE KEYS */;
INSERT INTO `mall_ak_sk`
VALUES (1, 'AKc9df44db23be4b98', 'SK17a9f829ad3e43ad938a8d62e40d93f3', 1);


CREATE TABLE `mall_cart`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT,
    `member_id`   bigint  NOT NULL,
    `product_id`  bigint  NOT NULL,
    `quantity`    int     NOT NULL,
    `selected`    tinyint NOT NULL                                              DEFAULT '0' COMMENT '//0-no 1-yes',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_product` (`member_id`, `product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_cart';


CREATE TABLE `mall_category`
(
    `id`            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT 'Category ID, Primary Key',
    `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Category Name',
    `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Category Code, Unique Identifier',
    `description`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Category Description',
    `image_url`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Category Image URL',
    `icon_url`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Category Icon URL',
    `status`        tinyint(1)                                                    DEFAULT '1' COMMENT 'Status: 0=Disabled, 1=Enabled',
    `sort_order`    int                                                           DEFAULT '0' COMMENT 'Sort Order, Lower Number = Higher Priority',
    `is_featured`   tinyint(1)                                                    DEFAULT '0' COMMENT 'Is Featured: 0=No, 1=Yes',
    `create_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT 'Created By',
    `create_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
    `update_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT 'Updated By',
    `update_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
    `remark`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    UNIQUE KEY `uk_category_name` (`category_name`),
    KEY             `idx_status` (`status`),
    KEY             `idx_sort_order` (`sort_order`),
    KEY             `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='Product Category Table';

INSERT INTO `mall_category`
VALUES (1, 'Digital Product',  'DIGITAL',  'Digital product category, including software, e-books, online courses and other digital goods',      'https://fastly.picsum.photos/id/83/200/300.jpg?hmac=avqtE9ZSAkPbFtYCXzxg4TeAA-fMWqX6jUQeWI_HjLc',   NULL, 1, 1, 0, 'system', '2025-07-09 23:26:39', '', '2025-07-09 23:40:02', 'Digital Product Category' ),
       (2, 'Physical Product', 'PHYSICAL', 'Physical product category, including clothing, electronics, home goods and other physical products', 'https://fastly.picsum.photos/id/1071/200/300.jpg?hmac=y09-AL4WisOkuQR4SOKzDWjPHWptbCDbEaFP0yJkKNY', NULL, 1, 2, 0, 'system', '2025-07-09 23:26:39', '', '2025-07-09 23:40:02', 'Physical Product Category');


CREATE TABLE `mall_coupon`
(
    `coupon_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name`        varchar(100) NOT NULL COMMENT '优惠券名称',
    `code`        varchar(50)  NOT NULL COMMENT '优惠券代码',
    `type`        tinyint      NOT NULL COMMENT '1- Full discount coupon/ 2- Discount coupon/ 3-No threshold coupon',
    `amount`      decimal(10, 2) DEFAULT NULL COMMENT 'Discount amount (full discount coupon/no threshold coupon)',
    `discount`    decimal(3, 2)  DEFAULT NULL COMMENT 'Discount ratio (discount coupon)',
    `min_point`   decimal(10, 2) DEFAULT NULL COMMENT 'Usage threshold (full discount coupon)',
    `start_time`  datetime     NOT NULL COMMENT '开始时间',
    `end_time`    datetime     NOT NULL COMMENT '结束时间',
    `total_count` int          NOT NULL COMMENT 'Issuance quantity',
    `used_count`  int            DEFAULT '0' COMMENT '已使用数量',
    `status`      tinyint        DEFAULT '1' COMMENT '0-disable/1-enable',
    `create_by`   varchar(64)    DEFAULT '' COMMENT '创建者',
    `create_time` datetime       DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64)    DEFAULT '' COMMENT '更新者',
    `update_time` datetime       DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500)   DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`coupon_id`),
    UNIQUE KEY `idx_code` (`code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='mall_coupon';


CREATE TABLE `mall_coupon_history`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `coupon_id`   bigint NOT NULL COMMENT '优惠券ID',
    `member_id`   bigint NOT NULL COMMENT '用户ID',
    `order_id`    bigint       DEFAULT NULL COMMENT '订单ID',
    `order_sn`    varchar(64)  DEFAULT NULL COMMENT '订单编号',
    `use_status`  tinyint      DEFAULT '0' COMMENT '使用状态(0:未使用,1:已使用,2:已过期)',
    `use_time`    datetime     DEFAULT NULL COMMENT '使用时间',
    `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY           `idx_member_id` (`member_id`),
    KEY           `idx_coupon_id` (`coupon_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 313
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='mall_coupon_history';


CREATE TABLE `mall_member`
(
    `member_id`         bigint                                                       NOT NULL AUTO_INCREMENT,
    `username`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `password`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `primary_contact`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `secondary_contact` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `tg_id`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `tg_username`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `source_type`       tinyint                                                      NOT NULL DEFAULT '0' COMMENT '//0->os,1->tg',
    `status`            tinyint                                                      NOT NULL DEFAULT '0' COMMENT '//1->del',
    `create_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '创建者',
    `create_time`       datetime                                                              DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '更新者',
    `update_time`       datetime                                                              DEFAULT NULL COMMENT '更新时间',
    `remark`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '备注',
    `linked_account`    bigint                                                                DEFAULT NULL COMMENT 'linked member_id',
    PRIMARY KEY (`member_id`),
    UNIQUE KEY `idx_username_tgId` (`username`, `tg_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7877953855
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_member';


CREATE TABLE `mall_member_benefit`
(
    `level_id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `level_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL,
    `point`             bigint                                                        NOT NULL DEFAULT '0',
    `des`               varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `fixed_discount`    decimal(10, 4)                                                NOT NULL DEFAULT '0.0000',
    `percent_discount`  decimal(10, 4)                                                NOT NULL DEFAULT '0.0000',
    `shipping_discount` decimal(10, 4)                                                NOT NULL DEFAULT '0.0000',
    `create_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT '' COMMENT '创建者',
    `create_time`       datetime                                                               DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT '' COMMENT '更新者',
    `update_time`       datetime                                                               DEFAULT NULL COMMENT '更新时间',
    `remark`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`level_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_member_benefit';

INSERT INTO `mall_member_benefit`
VALUES (1, 'Bronze',   0,   'Exclusive Discounts\n Community support',   0.0000,  0.0000, 0.0000, '', NULL, '', NULL, NULL       ),
       (2, 'Silver',   50,  '$10 Discount\n Live chat support',          10.0000, 0.0000, 0.0000, '', NULL, '', NULL, 'no update'),
       (3, 'Gold',     100, '$20 Discount \n Dedicated Account Manager', 20.0000, 0.0000, 0.0000, '', NULL, '', NULL, NULL       ),
       (4, 'Platinum', 200, '3% Discount \n Free shipping',              0.0000,  0.0300, 1.0000, '', NULL, '', NULL, NULL       ),
       (5, 'Diamond',  500, '5% Discount \n Free shipping',              0.0000,  0.0500, 1.0000, '', NULL, '', NULL, NULL       );

CREATE TABLE `mall_member_level`
(
    `info_id`       bigint         NOT NULL AUTO_INCREMENT,
    `member_id`     bigint         NOT NULL,
    `total_orders`  bigint         NOT NULL                                       DEFAULT '0',
    `current_level` tinyint        NOT NULL                                       DEFAULT '1',
    `current_point` decimal(10, 4) NOT NULL                                       DEFAULT '0.0000',
    `current_aud`   decimal(10, 4) NOT NULL                                       DEFAULT '0.0000',
    `last_level`    tinyint        NOT NULL                                       DEFAULT '1',
    `last_point`    decimal(10, 4) NOT NULL                                       DEFAULT '0.0000',
    `last_aud`      decimal(10, 4) NOT NULL                                       DEFAULT '0.0000',
    `create_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`   datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`   datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`info_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_member_level';



CREATE TABLE `mall_member_login_info`
(
    `info_id`     bigint  NOT NULL AUTO_INCREMENT,
    `member_id`   bigint  NOT NULL,
    `source_type` tinyint NOT NULL                                              DEFAULT '0' COMMENT '//0->os,1->tg',
    `msg`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`info_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 719
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='mall_member_login_info';



CREATE TABLE `mall_member_point_history`
(
    `history_id`  bigint         NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `member_id`   bigint         NOT NULL COMMENT '会员ID',
    `amount`      decimal(10, 2) NOT NULL COMMENT '金额',
    `points`      decimal(10, 4) NOT NULL COMMENT '增加的积分(金额的1%)',
    `note`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    `platform`    tinyint        NOT NULL COMMENT '平台来源：0-OS 1-TG',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`history_id`),
    KEY           `idx_member_id` (`member_id`),
    KEY           `idx_create_time` (`create_time`),
    KEY           `idx_platform` (`platform`),
    CONSTRAINT `fk_point_history_member` FOREIGN KEY (`member_id`) REFERENCES `mall_member` (`member_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='会员积分增加历史记录表';



CREATE TABLE `mall_order`
(
    `id`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
    `order_sn`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
    `member_id`       bigint                                                       NOT NULL COMMENT '会员ID',
    `member_level`    tinyint                                                      NOT NULL DEFAULT '1' COMMENT '会员等级',
    `total_amount`    decimal(10, 2)                                               NOT NULL COMMENT '订单总金额',
    `freight_amount`  decimal(10, 2)                                                        DEFAULT '0.00' COMMENT '运费金额',
    `coupon_amount`   decimal(10, 2)                                                        DEFAULT '0.00' COMMENT '优惠券抵扣金额',
    `discount_amount` decimal(10, 2)                                                        DEFAULT '0.00' COMMENT '会员折扣金额',
    `coupon_id`       bigint                                                                DEFAULT NULL COMMENT '优惠券ID',
    `status`          tinyint                                                      NOT NULL DEFAULT '0' COMMENT '订单状态：0-Pending 1-Paid 2-Fulfilled 3-Shipped 4-Cancelled 5-isDeleted',
    `isdispute`       tinyint(1)                                                   NOT NULL DEFAULT '0' COMMENT '争议状态：0-无争议 1-有争议',
    `dispute_time`    datetime                                                              DEFAULT NULL COMMENT '争议发起时间',
    `dispute_reason`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '争议原因',
    `dispute_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '争议发起人',
    `source_type`     tinyint                                                      NOT NULL COMMENT '订单来源：0-OS 1-TG 2',
    `close_time`      datetime                                                              DEFAULT NULL COMMENT '关闭时间',
    `finish_time`     datetime                                                              DEFAULT NULL COMMENT '完成时间',
    `create_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '创建者',
    `create_time`     datetime                                                              DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '更新者',
    `update_time`     datetime                                                              DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_order_sn` (`order_sn`),
    KEY               `idx_member_id` (`member_id`),
    KEY               `idx_create_time` (`create_time`),
    KEY               `idx_isdispute` (`isdispute`),
    KEY               `idx_dispute_time` (`dispute_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='订单主表';



CREATE TABLE `mall_order_item`
(
    `id`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单项ID',
    `order_id`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
    `order_sn`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
    `product_id`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `product_name`    varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'product_name',
    `product_image`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品图片',
    `product_spec`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品规格',
    `price`           decimal(10, 2)                                                DEFAULT NULL COMMENT 'price',
    `quantity`        int                                                          NOT NULL COMMENT '购买数量',
    `total_price`     decimal(10, 2)                                                DEFAULT NULL COMMENT '(price*quantity)',
    `coupon_amount`   decimal(10, 2)                                                DEFAULT '0.00' COMMENT '优惠券分摊金额',
    `discount_amount` decimal(10, 2)                                                DEFAULT '0.00' COMMENT '折扣分摊金额',
    `create_time`     datetime                                                     NOT NULL COMMENT '创建时间',
    `update_time`     datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `create_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `update_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    `model`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT 'model=amount',
    `sku`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT 'sku for individual ',
    `total_coin`      decimal(16, 8)                                                DEFAULT NULL COMMENT 'total_coin for item',
    PRIMARY KEY (`id`),
    KEY               `idx_order_id` (`order_id`),
    KEY               `idx_product_id` (`product_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `mall_order` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='订单商品表';



CREATE TABLE `mall_order_payment`
(
    `id`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付信息ID',
    `order_id`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联订单ID',
    `pay_type`         tinyint                                                      NOT NULL COMMENT '0: BTC; 1:USDT 2:XMR',
    `pay_amount`       decimal(16, 8)                                               NOT NULL COMMENT 'amount in AUD',
    `transaction_id`   varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方交易号',
    `payment_status`   tinyint                                                       DEFAULT '0' COMMENT '支付状态：0-未支付 1-支付成功 2-支付失败',
    `pay_time`         datetime                                                      DEFAULT NULL COMMENT '支付时间',
    `callback_time`    datetime                                                      DEFAULT NULL COMMENT '回调时间',
    `callback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '回调内容',
    `refund_amount`    decimal(10, 2)                                                DEFAULT '0.00' COMMENT '退款金额',
    `refund_time`      datetime                                                      DEFAULT NULL COMMENT '退款时间',
    `refund_reason`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款原因',
    `create_by`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`      datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`      datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`           varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    `totalcoin`        decimal(16, 8)                                                DEFAULT NULL COMMENT 'amount in coin',
    `paidcoin`         decimal(16, 8)                                                DEFAULT NULL COMMENT 'paid coin',
    `currency`         varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '0: AUD, 1:USD',
    `rate`             decimal(10, 2)                                                DEFAULT NULL COMMENT 'current rate from btcserver',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_order_id` (`order_id`),
    KEY                `idx_transaction_id` (`transaction_id`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `mall_order` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='订单支付信息表';



CREATE TABLE `mall_order_shipping`
(
    `id`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '收货信息ID',
    `order_id`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '关联订单ID',
    `receiver_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人姓名',
    `receiver_phone`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '收货人电话',
    `address_line1`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址行1',
    `address_line2`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '地址行2',
    `city`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '城市',
    `state`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '省/州',
    `post_code`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '邮编',
    `country`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '国家',
    `shipping_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '配送方式',
    `shipping_time`   datetime                                                       DEFAULT NULL COMMENT '发货时间',
    `shipping_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT NULL COMMENT '物流单号',
    `shipping_status` tinyint                                                        DEFAULT '0' COMMENT '配送状态：0-待发货 1-已发货 2-已收货',
    `create_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '创建者',
    `create_time`     datetime                                                       DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '更新者',
    `update_time`     datetime                                                       DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_shipping_order` FOREIGN KEY (`order_id`) REFERENCES `mall_order` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='订单收货信息表';



CREATE TABLE `mall_page_config`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `page`         varchar(32) NOT NULL,
    `section`      varchar(32) NOT NULL,
    `config_key`   varchar(64) NOT NULL,
    `config_value` text,
    `sort`         int          DEFAULT '0',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`    varchar(64)  DEFAULT NULL,
    `remark`       varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 82
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Mall page config table with audit fields and remark';

INSERT INTO `mall_page_config`
VALUES (1, 'profile', 'agreement', 'content', 'What\'s a loyalty program?\nOur loyalty program rewards customers for choosing us as your preferred vendor. You\'ll earn points based on your activity throughout the year, which will determine your loyalty level. Each year, your points reset, and different loyalty levels come with various privileges, including exclusive discounts and services. For more details, please refer to the information above.\n\nHow can I earn points?\nThere are several ways to earn points:\n\nComplete the beginner\'s package.\n\nMake Purchases: Earn 1% of your total purchases from our Tor or Telegram shop in points.\n\nRefer a Friend:\n\nShare our Tor or Telegram shop with a friend: you\'ll earn 5% of your friend\'s total AUD spend on their first purchase in points.\n\nRefer a friend to our Telegram Channel: Earn 1 point.\n\nHow long are my points valid for?\nPoints are valid until 31st December of each year. Your accumulated points will determine your loyalty level for the following year.\n\nCan I use my points?\nCurrently TP points are only used to determine your loyalty level for the next year. However, we are working on a new feature called \"Store Credit.\" which will allow you to convert points into AUD value for additional discounts. Stay tuned for more information!\n\nCan I cash out my points?\nNo, points cannot be cashed out.\n\nDo I need to sign up to join the program?\nYes, you must sign up to participate in the program. While we understand some customers prefer to order as guests for anonymity, registering allows us to track your order history and volume, which is essential for the program.\n\nHow can I see my points?\nYou can view your current points on this page. These points reflect your earnings for the current year and will influence your level for the following year. Points are automatically added to your account with each purchase. Any extra points earned through other activities will be manually added by our staff after verification.\n\nHow can I get discounts?\nWe are currently working on automating this process. In the meantime, you will receive a unique discount code that only you can use. For more details, please reach out to our friendly staff via Telegram at @TheProfessionals_CS2',
                                                                                                                                                                                                                                                                                                                                                  0,
                                                                                                                                                                                                                                                                                                                                                     '2025-07-14 08:52:30', '2025-07-16 23:46:33', NULL,    ''  ),
       (2, 'home',    'logo',      'images',  '{\"images\":[{\"url\":\"/profile/upload/2025/08/09/acat_20250809143328A002.jpeg\",\"sort\":0}],\"url\":\"http://jvikvn6ghcp5b7b3wgjcuckigrdxjr3n4qg62ijekf6xug27fjlirmid.onion/mall/static/home\"}',                                                                                               1, '2025-07-14 11:27:57', '2025-08-09 14:33:30', NULL,    NULL),
       (3, 'home',    'banner',    'images',  '{\"images\":[{\"url\":\"/profile/upload/2025/08/11/home%20banner2_20250811143554A002.jpg\",\"sort\":0},{\"url\":\"/profile/upload/2025/08/11/acat_20250811143604A003.jpeg\",\"sort\":1}],\"url\":\"http://jvikvn6ghcp5b7b3wgjcuckigrdxjr3n4qg62ijekf6xug27fjlirmid.onion/mall/static/products\"}', 2, '2025-07-14 11:27:57', '2025-08-11 14:36:10', NULL,    NULL),
       (4, 'home',    'digital',   'images',  '{\"images\":[{\"url\":\"/profile/upload/2025/08/09/home2_20250809152741A004.jpg\",\"sort\":0}],\"title\":\"Digital Products\",\"url\":\"http://jvikvn6ghcp5b7b3wgjcuckigrdxjr3n4qg62ijekf6xug27fjlirmid.onion/mall/static/products?category=Digital%20Product\"}',                                 3, '2025-07-14 12:40:35', '2025-08-09 15:27:57', NULL,    NULL),
       (5, 'home',    'physical',  'images',  '{\"images\":[{\"url\":\"/profile/upload/2025/08/09/home3_20250809152753A005.jpg\",\"sort\":0}],\"title\":\"Physical Products\",\"url\":\"http://jvikvn6ghcp5b7b3wgjcuckigrdxjr3n4qg62ijekf6xug27fjlirmid.onion/mall/static/products?category=Physical%20Product\"}',                               4, '2025-07-14 12:40:35', '2025-08-09 15:27:58', NULL,    NULL),
       (6, 'footer',  'logo',      'images',  '{\"images\":[{\"url\":\"/profile/upload/2025/08/09/footerlogofile2_20250809152830A006.png\",\"title\":\"footerlogofile2.png\"}],\"url\":\"http://jvikvn6ghcp5b7b3wgjcuckigrdxjr3n4qg62ijekf6xug27fjlirmid.onion/mall/static/home\"}',                                                              1, '2025-07-14 15:47:43', '2025-08-09 15:28:36', 'admin', NULL),
       (7, 'footer',  'contacts',  'list',    '[{\"name\":\"CON1\",\"url\":\"@CON1\"},{\"name\":\"CON2\",\"url\":\"@CON2\"},{\"name\":\"CON3\",\"url\":\"@CON3\"}]',                                                                                                                                                                              2, '2025-07-14 15:47:44', '2025-07-15 13:03:17', 'admin', NULL);



CREATE TABLE `mall_pgp_keys`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `key_name`    varchar(128) NOT NULL,
    `key_type`    enum ('public','private') NOT NULL,
    `key_id`      varchar(64)  NOT NULL,
    `fingerprint` varchar(128) NOT NULL,
    `key_data`    text         NOT NULL,
    `key_size`    int          DEFAULT '2048',
    `algorithm`   varchar(32)  DEFAULT 'RSA',
    `is_active`   tinyint(1)   DEFAULT '1',
    `is_default`  tinyint(1)   DEFAULT '0',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP,
    `expires_at`  datetime     DEFAULT NULL,
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`   varchar(64)  DEFAULT NULL,
    `remark`      varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_type_id` (`key_type`, `key_id`),
    KEY           `idx_key_type` (`key_type`),
    KEY           `idx_is_active` (`is_active`),
    KEY           `idx_is_default` (`is_default`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='PGP密钥表（服务器生成）';

INSERT INTO `mall_pgp_keys`
VALUES (5, 'PGP 71823 (Public)',  'public',  'E39E4573B0A23C72', '7F288DEF523BD4822F0C8C43E39E4573B0A23C72', 'mQENBGh6YuUBCACu3JC/hLUtQDXOmVgX9Bbk1GdtqydTyn3Dx/EKhQLDW6WSXoRhIEu5qY6kSnKmM2rw2ar3Prxiwr947nwrmtzo3C1FvoPE4w9BOYI464JdxAdQ9PKN7zfzPStp8uB7sA1see88q9O704exN2hYFA27cGe8Vow7YqBI2XbzPGqIIaj/1SU/axyJV/u0+2lxHincopJR5XSdxzd9f4trcpoVGLC9CcopArNdE+FArJXAfUvllsSo5v9ng0mS5GszQnKVJlYhOXEOwoWJMeKwMBQH+gTa9wGFIyyGnZ7sX7g/W22yd9Rsq3/tE6QKQGuQWwL8l2zAwU/u3ekCl9eBgvyNABEBAAG0HFBHUCA3MTgyMyA8YWRtaW5AbWVkdXNhLmNvbT6JAVMEEwEKAEcFAmh6YuUJEOOeRXOwojxyFiEEfyiN71I71IIvDIxD455Fc7CiPHICngECmwcFFgIDAQAECwkIBwUVCgkICwWJCWYBgAKZAQAAo44H/1zPbOiJ1HNypSU3unn963zQqfWqlHVtgWmNL9hVDXJrdERu9P5pk247lSk1bif8uxg2/l3fuTL9X0yMBuv7oB0hlVXKim56Xb860EmKcTFqc0ZDRzcXPvV+O3SGcPO9JKDSeUmwY3jcn0CpMGrIS/YCVNJbKRGtCfYf6+9efG40LRsZZxsPngvH5IcFwsiysVE5N2vbfGr3K9oD2FY/1tS/IGUXEYc4BXI+u3xDhd6cZ6hvJXDx8kN84OewkjnaiD+EUzi0NyImiplD37LTTib+xpq3SMdRzBYnBTnyiDEgQLY89hxpsKiL2cWfRG3idWuEhRmMIYmrn0rsqmGn1CA=', 2048, 'RSA', 1, 0, '2025-07-18 23:06:13', NULL, '2025-07-18 23:06:14', '2025-07-18 23:06:13', NULL, NULL),
       (6, 'PGP 71823 (Private)', 'private', 'E39E4573B0A23C72', '7F288DEF523BD4822F0C8C43E39E4573B0A23C72', 'lQOYBGh6YuUBCACu3JC/hLUtQDXOmVgX9Bbk1GdtqydTyn3Dx/EKhQLDW6WSXoRhIEu5qY6kSnKmM2rw2ar3Prxiwr947nwrmtzo3C1FvoPE4w9BOYI464JdxAdQ9PKN7zfzPStp8uB7sA1see88q9O704exN2hYFA27cGe8Vow7YqBI2XbzPGqIIaj/1SU/axyJV/u0+2lxHincopJR5XSdxzd9f4trcpoVGLC9CcopArNdE+FArJXAfUvllsSo5v9ng0mS5GszQnKVJlYhOXEOwoWJMeKwMBQH+gTa9wGFIyyGnZ7sX7g/W22yd9Rsq3/tE6QKQGuQWwL8l2zAwU/u3ekCl9eBgvyNABEBAAEAB/0fPOR37lF+grrKDG9mIhMn8IEEoL+PErzKabDYMABZHYlz30I3ZrFYet+yXmFiawuW+M+3ySOW4hMaG8KfeJQv3kuRKtJ37RdHz+oBWKNKtG8ZKZUeQYbFg586l2q+depD/3l7ZXfXFVgnks4nc/erDU7e4Lg/EKSxNvYuyFOAu3TO2I97DjENcUl7+Q1R1EovDZ6Ky4jh7y07W63tb/jf4nxcUHFpFr6Rc4V0bnzBbo90uBiWmiiLKEVKSnGM8XYcBtA3zGcdt8xs3X79ZXGuif9pNp225e93n/SZUNEb+94kaPrJKdWGrNNHyoZ1DEF8npkgCm3dyEormk3/zHnZBADCyol+6vDCwwZs6VumN5oH2p97jB6kcnR/wJx6Ke91y28iCnhpIbYHKZ/RqNsfvn9SNAAi5nFOGPJy0JlH9+DUyTh59fpYuHVdkZSogh/3Hr8PxaQ5GkTsvPlqveJDvKWLFyVKOLRzzPUE/83r3CUU0BqNMmkobX/g/EEwxTTWKQQA5c7YjThQ//75/fpExFN2iv1BTPras6VZhnqQdn9sE886PnAT/so8V0UMRtPFKxNDwdpmX2dAeS/N+LW3LMHLw1ctlpQRmXwzxxTd792esWnUloUQElysh3Zuvg0D6PCQp+Km0q9n9sdaWrJPa6ryIQlXjy34Kkyu/wYancpIl8UD/1zAbaqn11mgHXwqzJ8W9sZILGbzD5zoHDkm11Ck0VM+5GQF4UHDqQvAmQyyQQ3QiBesQKLj3OEVlDgJUlpU2gCADaBZYeLOq//BoTx44z18OPqfwZGC8V7ZH2evrUD1RX4ps9wigUD/1BC3xyoHcP6ycekQyxOGotaqRZ+Za+FPSkO0HFBHUCA3MTgyMyA8YWRtaW5AbWVkdXNhLmNvbT6JAVMEEwEKAEcFAmh6YuUJEOOeRXOwojxyFiEEfyiN71I71IIvDIxD455Fc7CiPHICngECmwcFFgIDAQAECwkIBwUVCgkICwWJCWYBgAKZAQAAo44H/1zPbOiJ1HNypSU3unn963zQqfWqlHVtgWmNL9hVDXJrdERu9P5pk247lSk1bif8uxg2/l3fuTL9X0yMBuv7oB0hlVXKim56Xb860EmKcTFqc0ZDRzcXPvV+O3SGcPO9JKDSeUmwY3jcn0CpMGrIS/YCVNJbKRGtCfYf6+9efG40LRsZZxsPngvH5IcFwsiysVE5N2vbfGr3K9oD2FY/1tS/IGUXEYc4BXI+u3xDhd6cZ6hvJXDx8kN84OewkjnaiD+EUzi0NyImiplD37LTTib+xpq3SMdRzBYnBTnyiDEgQLY89hxpsKiL2cWfRG3idWuEhRmMIYmrn0rsqmGn1CA=', 2048, 'RSA', 1, 0, '2025-07-18 23:06:13', NULL, '2025-07-18 23:06:14', '2025-07-18 23:06:13', NULL, NULL),
       (7, 'PGP 71824 (Public)',  'public',  '4BEDA5C854965133', 'ADD43B835096923089A912A34BEDA5C854965133', 'mQENBGh6bisBCADW/4emk2DhrxiAz/GPAtwX2Yl8vFOMP/GSKK1zY/eD9tKsi+0TGEypDQGO4xqha2JKzm5irs4ZeXhyWQeBzN63CyGkwfGGlEeRwqtcOC6ojrMRMiQGwjiRSQxWzSwhmiyNq1yQF0D/dndfYzOpIw5DL2lZr+gFKjm/l8xMXhk0dz44Dp2BV+drfwXGU4vFrmq7E+6yq3pTTRD6DXWWdxPLRqM2swtlooo3ppFaAfsqTm9zICMuGGHSMK+eympnODAmB0IZ4Sn2vMOLLaiRV2l7PsnC21ILQ+z6GAQA9hnKH3gN6vsQR/hJtuCtiRNdr61ssFSHOLxrlhTijA/9wrEPABEBAAG0HFBHUCA3MTgyNCA8YWRtaW5AbWVkdXNhLmNvbT6JAVMEEwEKAEcFAmh6bisJEEvtpchUllEzFiEErdQ7g1CWkjCJqRKjS+2lyFSWUTMCngECmwcFFgIDAQAECwkIBwUVCgkICwWJCWYBgAKZAQAAgEsIALZL83yisOGGf6RR2rnJbS9vkIXL0n2YKsKlsZTceBjFXU5p33Vft+fUrjs8tfylAiLG/Dz+DNbZv+LE8LQGmr08IARuOwBicd1+VCsBwMagOuVvzTdNde+JF+1iKnCZl0/PaM0qwBDwr43yweW0q9VMbSO2YZB8YV0trGW+OOlgHvfdHar8GcGC4Nn1DZlyLxlUYfLXo9wO2YQHeCiRF5C4pNtthXUGldtv8W/VRaH8poEitB4tTxtNqQqXfKeE7CB5BIJ09UlJbL4Lbg6LfugnOcYTMtT0f1E4iseqf1PO0vtmH5Cn5+x14QY+HR9V1wg3Zg6xIbMlJPoZYzZCtU4=', 2048, 'RSA', 1, 1, '2025-07-18 23:54:19', NULL, '2025-07-18 23:54:19', '2025-07-19 07:49:32', 'admin', NULL),
       (8, 'PGP 71824 (Private)', 'private', '4BEDA5C854965133', 'ADD43B835096923089A912A34BEDA5C854965133', 'lQOYBGh6bisBCADW/4emk2DhrxiAz/GPAtwX2Yl8vFOMP/GSKK1zY/eD9tKsi+0TGEypDQGO4xqha2JKzm5irs4ZeXhyWQeBzN63CyGkwfGGlEeRwqtcOC6ojrMRMiQGwjiRSQxWzSwhmiyNq1yQF0D/dndfYzOpIw5DL2lZr+gFKjm/l8xMXhk0dz44Dp2BV+drfwXGU4vFrmq7E+6yq3pTTRD6DXWWdxPLRqM2swtlooo3ppFaAfsqTm9zICMuGGHSMK+eympnODAmB0IZ4Sn2vMOLLaiRV2l7PsnC21ILQ+z6GAQA9hnKH3gN6vsQR/hJtuCtiRNdr61ssFSHOLxrlhTijA/9wrEPABEBAAEAB/9FYxzFfuUSkS8IS5Q7QoID1HpR3nncVrrBIA2yov28AknF1DcEJaCAIqlxEwyCMiT73Zm0kZQElrZvuRn6I0Nxsc7+3HT1qR9cBIbHcX5legCCuTr1T3NwODIaRYdLpPViF379JfQdPn+SMnL20FCHEGMgxevM1u7iTbxqnSfnTUk9nXtHIFqQciRmBLDF2aU6mv2XnPDP7und2sKwM1SW9jvuXyVKkCHFYH3+qztqkR4cdQXaRqIwrNTpStXFQIIjEl+gV6+emZ2eXKNo0ZH4dRQ6nJGhsGL2McOzfGZJb+DyB1zqT8SrBXVAzzMZ2SiL2+sJS8o/rfht/4zsuAxZBADnPV5JNSdLQRCifPvw2Zxxt/siAHuFHsGwDLh2D4KAsMpTp6wYrCQ2jsCRORwOpQiqIntX+8A7LQ2PMm3Ic7LukDWRyZ0hnGShU1lOJQz7C+q+UlUZNYxZWJ2TEJhFW/c4vObZC8xXuT5UcbETRNH4YUcJJBgOgnYocsusiWk1cwQA7gT0s/S1688Q2rJ4vza+UA6axq94Px9d4lofUBN55YlX20LujTqF75H4qNYk/nK/cEHT1gmopTUCt/gTM0OhPytscnG0K+jMir5ke9v7ShyyjVyXpeVXqT2RUWTyGYr/avNU7I9+rXHofT8YFtbEF27rk8Z8qLA5R2sj7ZywzvUD/2TmTyJE8K8uctp0Flobb83pzP4RIasDnE9ed59flq1JT6nRUU93WNgm4zBCKnnZKI6QCn1CRhVViBrYz/bKQYrzT1KMlFCeI2mn2SO17+TE4eZFrGzES9YkoiomIA7uTxRYFgjLqCSJ6O+8P/02drMIxhRzRA+T9E2XiWnI4aJnPu60HFBHUCA3MTgyNCA8YWRtaW5AbWVkdXNhLmNvbT6JAVMEEwEKAEcFAmh6bisJEEvtpchUllEzFiEErdQ7g1CWkjCJqRKjS+2lyFSWUTMCngECmwcFFgIDAQAECwkIBwUVCgkICwWJCWYBgAKZAQAAgEsIALZL83yisOGGf6RR2rnJbS9vkIXL0n2YKsKlsZTceBjFXU5p33Vft+fUrjs8tfylAiLG/Dz+DNbZv+LE8LQGmr08IARuOwBicd1+VCsBwMagOuVvzTdNde+JF+1iKnCZl0/PaM0qwBDwr43yweW0q9VMbSO2YZB8YV0trGW+OOlgHvfdHar8GcGC4Nn1DZlyLxlUYfLXo9wO2YQHeCiRF5C4pNtthXUGldtv8W/VRaH8poEitB4tTxtNqQqXfKeE7CB5BIJ09UlJbL4Lbg6LfugnOcYTMtT0f1E4iseqf1PO0vtmH5Cn5+x14QY+HR9V1wg3Zg6xIbMlJPoZYzZCtU4=', 2048, 'RSA', 1, 1, '2025-07-18 23:54:19', NULL, '2025-07-18 23:54:19', '2025-07-19 07:49:35', 'admin', NULL);



CREATE TABLE `mall_pgp_encrypted_address`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT,
    `order_id`          varchar(64) NOT NULL,
    `user_id`           bigint      NOT NULL,
    `key_id`            bigint      NOT NULL,
    `encrypted_address` text        NOT NULL,
    `original_hash`     varchar(128) DEFAULT NULL,
    `encryption_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
    `create_time`       datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark`            varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY                 `idx_user_id` (`user_id`),
    KEY                 `idx_key_id` (`key_id`),
    KEY                 `idx_encryption_time` (`encryption_time`),
    CONSTRAINT `mall_pgp_encrypted_address_ibfk_1` FOREIGN KEY (`key_id`) REFERENCES `mall_pgp_keys` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='PGP加密地址表';



CREATE TABLE `mall_product`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `product_id`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Product ID (logical identifier)',
    `category`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Product category',
    `category_id` bigint                                                        DEFAULT NULL COMMENT 'Category ID, references mall_category table',
    `name`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Product name',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Product description',
    `image_url`   varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Product image URL',
    `status`      tinyint                                                       DEFAULT '1' COMMENT 'Product status (0=inactive, 1=active)',
    `shop_id`     bigint                                                        DEFAULT '0' COMMENT 'Shop ID',
    `channel`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT 'OS/TG' COMMENT 'Sales channel (OS=Online Store, TG=Telegram, OS/TG=both etc.)',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT 'Created by',
    `create_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT 'Updated by',
    `update_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`),
    KEY           `idx_category` (`category`),
    KEY           `idx_status` (`status`),
    KEY           `idx_channel` (`channel`),
    KEY           `idx_category_id` (`category_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='Product master table';


CREATE TABLE `mall_product2`
(
    `id`         bigint                                                       NOT NULL AUTO_INCREMENT,
    `product_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `sku`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `model`      int                                                          NOT NULL,
    `unit`       varchar(16) COLLATE utf8mb4_general_ci                       NOT NULL DEFAULT 'g' COMMENT 'Unit for amount (g, ml, pills, ea)',
    `price`      decimal(10, 3)                                               NOT NULL DEFAULT '0.000',
    `currency`   varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'AUD',
    `inventory`  int                                                          NOT NULL DEFAULT '0',
    `status`     tinyint                                                               DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku` (`sku`),
    KEY          `idx_product_id` (`product_id`),
    CONSTRAINT `fk_product2_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`product_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 24
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='Product and SKU information table';



CREATE TABLE `mall_product2_backup`
(
    `id`          bigint                                                        NOT NULL DEFAULT '0',
    `category`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `shop_id`     bigint                                                                 DEFAULT '0',
    `product_id`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `name`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `sku`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `model`       int                                                           NOT NULL,
    `price`       decimal(10, 3)                                                NOT NULL DEFAULT '0.000',
    `currency`    varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL DEFAULT 'AUD',
    `inventory`   int                                                           NOT NULL DEFAULT '0',
    `status`      tinyint                                                                DEFAULT '0',
    `des`         varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          DEFAULT '',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci           DEFAULT NULL,
    `create_time` datetime                                                               DEFAULT CURRENT_TIMESTAMP,
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci           DEFAULT NULL,
    `update_time` datetime                                                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          DEFAULT NULL,
    `image_url`   varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          DEFAULT NULL COMMENT 'Product image URL'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;



CREATE TABLE `mall_shipping_method`
(
    `id`          varchar(36)    NOT NULL COMMENT 'Primary Key ID',
    `code`        varchar(10)    NOT NULL COMMENT 'Shipping Method Code',
    `name`        varchar(50)    NOT NULL COMMENT 'Shipping Method Name',
    `fee`         decimal(10, 2) NOT NULL COMMENT 'Shipping Fee',
    `status`      tinyint(1) DEFAULT '1' COMMENT 'Status (0: Disabled, 1: Enabled)',
    `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
    `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Shipping Method Table';

INSERT INTO `mall_shipping_method`
VALUES ('f78243c0-4a77-11f0-8959-af134d4b92f8', 'RP',   'Regular Post',           10.00, 1, '2025-06-16 14:05:56', '2025-06-16 14:05:56'),
       ('f7847b18-4a77-11f0-8959-af134d4b92f8', 'EP',   'Express Post',           17.00, 1, '2025-06-16 14:05:56', '2025-06-16 14:05:56'),
       ('f786feb0-4a77-11f0-8959-af134d4b92f8', 'RPWG', 'Regular Post with Gift', 25.00, 1, '2025-06-16 14:05:56', '2025-06-16 14:05:56'),
       ('f788360e-4a77-11f0-8959-af134d4b92f8', 'EPWG', 'Express Post with Gift', 30.00, 1, '2025-06-16 14:05:56', '2025-06-16 14:05:56'),
       ('f789ae8a-4a77-11f0-8959-af134d4b92f8', 'FP',   'Full Package',           35.00, 1, '2025-06-16 14:05:56', '2025-06-16 14:05:56');


CREATE TABLE `sys_config`
(
    `config_id`    int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
    `config_name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
    `config_key`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键名',
    `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键值',
    `config_type`  char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
    `create_by`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`  datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`  datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='参数配置表';

INSERT INTO `sys_config`
VALUES (1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',         'skin-blue',  'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),
       (2, '用户管理-账号初始密码',         'sys.user.initPassword',      '123456',     'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '初始化密码 123456'                                                             ),
       (3, '主框架页-侧边栏主题',           'sys.index.sideTheme',        'theme-dark', 'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '深色主题theme-dark，浅色主题theme-light'                                        ),
       (4, '账号自助-验证码开关',           'sys.account.captchaEnabled', 'true',       'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '是否开启验证码功能（true开启，false关闭）'                                        ),
       (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',   'false',      'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '是否开启注册用户功能（true开启，false关闭）'                                      ),
       (6, '用户登录-黑名单列表',           'sys.login.blackIPList',      '',           'Y', 'admin', '2025-05-27 10:47:09', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'                   );


CREATE TABLE `sys_dept`
(
    `dept_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
    `parent_id`   bigint                                                       DEFAULT '0' COMMENT '父部门id',
    `ancestors`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
    `dept_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
    `order_num`   int                                                          DEFAULT '0' COMMENT '显示顺序',
    `leader`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
    `phone`       varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
    `email`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
    `del_flag`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                     DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dept_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 205
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='部门表';

INSERT INTO `sys_dept`
VALUES (100, 0,   '0',     'medusa',     0, 'admin', '15888888888', NULL, '0', '0', 'admin', '2025-05-27 10:47:08', 'admin', '2025-08-18 00:00:00'),
       (101, 100, '0,100', 'admin',      0, NULL,    NULL,          NULL, '0', '0', 'admin', '2025-08-18 00:00:00', '',      NULL                 ),
       (102, 100, '0,100', 'accountant', 1, NULL,    NULL,          NULL, '0', '0', 'admin', '2025-08-18 00:00:00', '',      NULL                 ),
       (103, 100, '0,100', 'operator',   2, NULL,    NULL,          NULL, '0', '0', 'admin', '2025-08-18 00:00:00', '',      NULL                 ),
       (104, 100, '0,100', 'warehouse1', 3, NULL,    NULL,          NULL, '0', '0', 'admin', '2025-06-04 22:07:23', '',      NULL                 );

CREATE TABLE `sys_dict_data`
(
    `dict_code`   bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    `dict_sort`   int                                                           DEFAULT '0' COMMENT '字典排序',
    `dict_label`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典标签',
    `dict_value`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典键值',
    `dict_type`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
    `css_class`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
    `list_class`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表格回显样式',
    `is_default`  char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dict_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 114
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='字典数据表';

INSERT INTO `sys_dict_data`
VALUES (1,   1,  '男',            '0',       'sys_user_sex',       '',   '',        'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '性别男'                    ),
       (2,   2,  '女',            '1',       'sys_user_sex',       '',   '',        'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '性别女'                    ),
       (3,   3,  '未知',          '2',       'sys_user_sex',       '',   '',        'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '性别未知'                  ),
       (4,   1,  '显示',          '0',       'sys_show_hide',      '',   'primary', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '显示菜单'                  ),
       (5,   2,  '隐藏',          '1',       'sys_show_hide',      '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '隐藏菜单'                  ),
       (6,   1,  'Normal',        '0',       'sys_normal_disable', '',   'primary', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '正常状态'                  ),
       (7,   2,  'Disable',       '1',       'sys_normal_disable', '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '停用状态'                  ),
       (8,   1,  'Normal',        '0',       'sys_job_status',     '',   'primary', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '正常状态'                  ),
       (9,   2,  'Pending',       '1',       'sys_job_status',     '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '停用状态'                  ),
       (10,  1,  'Default',       'DEFAULT', 'sys_job_group',      '',   '',        'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '默认分组'                  ),
       (11,  2,  'System',        'SYSTEM',  'sys_job_group',      '',   '',        'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '系统分组'                  ),
       (12,  1,  '是',            'Y',       'sys_yes_no',         '',   'primary', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '系统默认是'                ),
       (13,  2,  '否',            'N',       'sys_yes_no',         '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '系统默认否'                ),
       (14,  1,  '通知',          '1',       'sys_notice_type',    '',   'warning', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '通知'                      ),
       (15,  2,  '公告',          '2',       'sys_notice_type',    '',   'success', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '公告'                      ),
       (16,  1,  '正常',          '0',       'sys_notice_status',  '',   'primary', 'Y', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '正常状态'                  ),
       (17,  2,  '关闭',          '1',       'sys_notice_status',  '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '关闭状态'                  ),
       (18,  99, '其他',          '0',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '其他操作'                  ),
       (19,  1,  '新增',          '1',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '新增操作'                  ),
       (20,  2,  '修改',          '2',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '修改操作'                  ),
       (21,  3,  '删除',          '3',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '删除操作'                  ),
       (22,  4,  '授权',          '4',       'sys_oper_type',      '',   'primary', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '授权操作'                  ),
       (23,  5,  '导出',          '5',       'sys_oper_type',      '',   'warning', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '导出操作'                  ),
       (24,  6,  '导入',          '6',       'sys_oper_type',      '',   'warning', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '导入操作'                  ),
       (25,  7,  '强退',          '7',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '强退操作'                  ),
       (26,  8,  '生成代码',      '8',       'sys_oper_type',      '',   'warning', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '生成操作'                  ),
       (27,  9,  '清空数据',      '9',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '清空操作'                  ),
       (28,  1,  '成功',          '0',       'sys_common_status',  '',   'primary', 'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '正常状态'                  ),
       (29,  2,  '失败',          '1',       'sys_common_status',  '',   'danger',  'N', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '停用状态'                  ),
       (38,  3,  'Other',         '2',       'source_type',        '',   'warning', 'N', '0', 'admin', '2025-06-02 14:41:53', '',      NULL,                  'Other Source'              ),
       (50,  1,  'Fixed Amount',  '1',       'coupon_type',        '',   'primary', 'Y', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Fixed amount coupon'       ),
       (51,  2,  'Percentage',    '2',       'coupon_type',        '',   'success', 'N', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Percentage discount coupon'),
       (52,  3,  'Free Shipping', '3',       'coupon_type',        '',   'warning', 'N', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Free shipping coupon'      ),
       (60,  1,  'Not Started',   '0',       'coupon_status',      '',   'info',    'Y', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Coupon not started'        ),
       (61,  2,  'Active',        '1',       'coupon_status',      '',   'primary', 'N', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Coupon is active'          ),
       (62,  3,  'Expired',       '2',       'coupon_status',      '',   'danger',  'N', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Coupon has expired'        ),
       (63,  4,  'Disabled',      '3',       'coupon_status',      '',   'warning', 'N', '0', 'admin', '2025-05-28 13:36:46', '',      NULL,                  'Coupon is disabled'        ),
       (64,  1,  'Pending',       '0',       'order_status',       '',   'warning', 'Y', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Pending Status'            ),
       (65,  2,  'Paid',          '1',       'order_status',       '',   'info',    'N', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Paid Status'               ),
       (66,  3,  'Fulfilled',     '2',       'order_status',       '',   'primary', 'N', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Fulfilled Status'          ),
       (67,  4,  'Shipped',       '3',       'order_status',       '',   'success', 'N', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Shipped Status'            ),
       (68,  5,  'Cancelled',     '4',       'order_status',       '',   'danger',  'N', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Cancelled Status'          ),
       (69,  6,  'isDeleted',     '5',       'order_status',       '',   'info',    'N', '0', 'admin', '2025-06-27 22:23:00', '',      NULL,                  'Deleted Status'            ),
       (70,  1,  'OS',            '0',       'source_type',        '',   'info',    'Y', '0', 'admin', '2025-06-02 15:21:59', '',      NULL,                  'OS Source'                 ),
       (71,  2,  'Telegram',      '1',       'source_type',        '',   'success', 'N', '0', 'admin', '2025-06-02 15:21:59', 'admin', '2025-06-06 12:14:32', 'TG Source'                 ),
       (72,  3,  'Other',         '2',       'source_type',        '',   'warning', 'N', '0', 'admin', '2025-06-02 15:11:43', '',      NULL,                  'Other Source'              ),
       (100, 0,  'BTC',           '0',       'pay_type',           NULL, 'default', 'N', '0', 'admin', '2025-06-04 22:04:16', '',      NULL,                  'BTC'                       ),
       (101, 1,  'USDT',          '1',       'pay_type',           NULL, 'default', 'N', '0', 'admin', '2025-06-04 22:04:43', '',      NULL,                  NULL                        ),
       (102, 2,  'XMR',           '2',       'pay_type',           NULL, 'default', 'N', '0', 'admin', '2025-06-04 22:05:17', '',      NULL,                  NULL                        ),
       (103, 0,  'Bronze',        '1',       'member_level',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:52:18', '',      NULL,                  NULL                        ),
       (104, 1,  'Silver',        '2',       'member_level',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:52:41', '',      NULL,                  NULL                        ),
       (105, 2,  'Gold',          '3',       'member_level',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:53:22', '',      NULL,                  NULL                        ),
       (106, 3,  'Platinum',      '4',       'member_level',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:53:42', '',      NULL,                  NULL                        ),
       (107, 4,  'Diamond',       '5',       'member_level',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:54:01', '',      NULL,                  NULL                        ),
       (108, 0,  'OS',            '0',       'channel_type',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:55:38', '',      NULL,                  NULL                        ),
       (109, 0,  'TG',            '1',       'channel_type',       NULL, 'default', 'N', '0', 'admin', '2025-06-06 01:55:53', 'admin', '2025-06-23 23:18:56', NULL                        ),
       (110, 0,  'Activated',     '1',       'product_status',     NULL, 'default', 'N', '0', 'admin', '2025-06-17 16:43:55', 'admin', '2025-06-24 12:46:43', NULL                        ),
       (111, 1,  'Deactivated',   '0',       'product_status',     NULL, 'default', 'N', '0', 'admin', '2025-06-17 16:44:14', 'admin', '2025-06-24 12:47:10', NULL                        ),
       (112, 2,  'isDelete',      '2',       'product_status',     NULL, 'default', 'N', '0', 'admin', '2025-06-17 17:42:56', '',      NULL,                  NULL                        ),
       (113, 3,  'OS/TG',         '3',       'channel_type',       NULL, 'default', 'N', '0', 'admin', '2025-06-23 23:18:20', '',      NULL,                  'both for OS and TG'        );


CREATE TABLE `sys_dict_type`
(
    `dict_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
    `dict_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典名称',
    `dict_type`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dict_id`),
    UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 104
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='字典类型表';

INSERT INTO `sys_dict_type`
VALUES (1,   '用户性别',       'sys_user_sex',       '0', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '用户性别列表'                                        ),
       (2,   '菜单状态',       'sys_show_hide',      '0', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '菜单状态列表'                                        ),
       (3,   '系统开关',       'sys_normal_disable', '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '系统开关列表'                                        ),
       (4,   '任务状态',       'sys_job_status',     '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '任务状态列表'                                        ),
       (5,   '任务分组',       'sys_job_group',      '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '任务分组列表'                                        ),
       (6,   '系统是否',       'sys_yes_no',         '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '系统是否列表'                                        ),
       (7,   '通知类型',       'sys_notice_type',    '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '通知类型列表'                                        ),
       (8,   '通知状态',       'sys_notice_status',  '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '通知状态列表'                                        ),
       (9,   '操作类型',       'sys_oper_type',      '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '操作类型列表'                                        ),
       (10,  '系统状态',       'sys_common_status',  '0', 'admin', '2025-05-27 10:47:09', '',      NULL,                  '登录状态列表'                                        ),
       (11,  'Order Status',   'order_status',       '0', 'admin', '2025-06-02 15:21:58', '',      NULL,                  'Order Status List'                                   ),
       (12,  'Order Source',   'source_type',        '0', 'admin', '2025-06-02 15:21:58', '',      NULL,                  'Order Source List'                                   ),
       (100, 'Pay Type',       'pay_type',           '0', 'admin', '2025-06-04 22:01:17', '',      NULL,                  'pay type list'                                       ),
       (101, 'Member Level',   'member_level',       '0', 'admin', '2025-06-06 01:51:41', '',      NULL,                  'member level '                                       ),
       (102, 'Channel Type',   'channel_type',       '0', 'admin', '2025-06-06 01:55:19', 'admin', '2025-06-06 01:57:41', NULL                                                  ),
       (103, 'Product Status', 'product_status',     '0', 'admin', '2025-06-17 16:43:26', 'admin', '2025-07-13 16:35:49', 'Product Status, 0,Deactivated,1,Activated,2,isDelete');



CREATE TABLE `sys_job`
(
    `job_id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `job_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT '' COMMENT '任务名称',
    `job_group`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    `invoke_target`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
    `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT 'cron执行表达式',
    `misfire_policy`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    `concurrent`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci               DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
    `status`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci               DEFAULT '0' COMMENT '状态（0正常 1暂停）',
    `create_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT '' COMMENT '创建者',
    `create_time`     datetime                                                               DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci           DEFAULT '' COMMENT '更新者',
    `update_time`     datetime                                                               DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT '' COMMENT '备注信息',
    PRIMARY KEY (`job_id`, `job_name`, `job_group`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 101
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='定时任务调度表';

INSERT INTO `sys_job`
VALUES (1,   '系统默认（无参）',        'DEFAULT', 'ryTask.ryNoParams',                                          '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-05-27 10:47:09', '', NULL, ''                                                                  ),
       (2,   '系统默认（有参）',        'DEFAULT', 'ryTask.ryParams(\'ry\')',                                    '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-05-27 10:47:09', '', NULL, ''                                                                  ),
       (3,   '系统默认（多参）',        'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-05-27 10:47:09', '', NULL, ''                                                                  ),
       (100, 'Order Timeout Handler', 'DEFAULT', 'orderTimeoutJob.processExpiredOrders()',                     '0 */5 * * * ?',  '1', '1', '0', 'admin', '2025-06-27 12:10:54', '', NULL, 'Process expired orders and release inventory locks every 5 minutes');



CREATE TABLE `sys_job_log`
(
    `job_log_id`     bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
    `job_name`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '任务名称',
    `job_group`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '任务组名',
    `invoke_target`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
    `job_message`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '日志信息',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci       DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
    `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
    `create_time`    datetime                                                       DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`job_log_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 13988
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='定时任务调度日志表';



CREATE TABLE `sys_logininfor`
(
    `info_id`        bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
    `user_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '用户账号',
    `ipaddr`         varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录IP地址',
    `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录地点',
    `browser`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '浏览器类型',
    `os`             varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '操作系统',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
    `msg`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提示消息',
    `login_time`     datetime                                                      DEFAULT NULL COMMENT '访问时间',
    PRIMARY KEY (`info_id`),
    KEY              `idx_sys_logininfor_s` (`status`),
    KEY              `idx_sys_logininfor_lt` (`login_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 263
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='系统访问记录';



CREATE TABLE `sys_menu`
(
    `menu_id`     bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
    `parent_id`   bigint                                                        DEFAULT '0' COMMENT '父菜单ID',
    `order_num`   int                                                           DEFAULT '0' COMMENT '显示顺序',
    `path`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由地址',
    `component`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
    `query`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
    `route_name`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '路由名称',
    `is_frame`    int                                                           DEFAULT '1' COMMENT '是否为外链（0是 1否）',
    `is_cache`    int                                                           DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
    `menu_type`   char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `visible`     char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `perms`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
    `icon`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '菜单图标',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`menu_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2042
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='菜单权限表';

INSERT INTO `sys_menu`
VALUES (1,    'system',                 0,    4,  'system',              NULL,                             '',   '', 1, 0, 'M', '0', '0', '',                            'system',     'admin', '2025-05-27 10:47:08', 'admin', '2025-06-11 18:46:31', 'system folder'                     ),
       (2, 'monitor', 0, 4, 'monitor', NULL, '', '', 1, 0, 'M', '1', '0', '', 'monitor', 'admin',                                                                                               '2025-05-27 10:47:08', 'admin', '2025-06-11 18:45:19', 'monitor folder'                    ),
       (3,    'tool',                   0,    3,  'tool',                NULL,                             '',   '', 1, 0, 'M', '1', '0', '',                            'tool',       'admin', '2025-05-27 10:47:08', 'admin', '2025-06-11 18:45:10', 'tool folder'                       ),
       (100,  'user',                   1,    1,  'user',                'system/user/index',              '',   '', 1, 0, 'C', '0', '0', 'system:user:list',            'user',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '用户管理菜单'                      ),
       (101,  'role',                   1,    2,  'role',                'system/role/index',              '',   '', 1, 0, 'C', '0', '0', 'system:role:list',            'peoples',    'admin', '2025-05-27 10:47:08', '',      NULL,                  '角色管理菜单'                      ),
       (102,  'menu',                   1,    3,  'menu',                'system/menu/index',              '',   '', 1, 0, 'C', '0', '0', 'system:menu:list',            'tree-table', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '菜单管理菜单'                      ),
       (103,  'dept',                   1,    4,  'dept',                'system/dept/index',              '',   '', 1, 0, 'C', '0', '0', 'system:dept:list',            'tree',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '部门管理菜单'                      ),
       (104,  'post',                   1,    5,  'post',                'system/post/index',              '',   '', 1, 0, 'C', '0', '0', 'system:post:list',            'post',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '岗位管理菜单'                      ),
       (105,  'dict',                   1,    6,  'dict',                'system/dict/index',              '',   '', 1, 0, 'C', '0', '0', 'system:dict:list',            'dict',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '字典管理菜单'                      ),
       (106,  'config',                 1,    7,  'config',              'system/config/index',            '',   '', 1, 0, 'C', '0', '0', 'system:config:list',          'edit',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '参数设置菜单'                      ),
       (107,  'notice',                 1,    8,  'notice',              'system/notice/index',            '',   '', 1, 0, 'C', '0', '0', 'system:notice:list',          'message',    'admin', '2025-05-27 10:47:08', '',      NULL,                  '通知公告菜单'                      ),
       (108,  'log',                    1,    9,  'log',                 '',                               '',   '', 1, 0, 'M', '0', '0', '',                            'log',        'admin', '2025-05-27 10:47:08', '',      NULL,                  '日志管理菜单'                      ),
       (109,  'online',                 2,    1,  'online',              'monitor/online/index',           '',   '', 1, 0, 'C', '0', '0', 'monitor:online:list',         'online',     'admin', '2025-05-27 10:47:08', '',      NULL,                  '在线用户菜单'                      ),
       (110,  'job',                    2,    2,  'job',                 'monitor/job/index',              '',   '', 1, 0, 'C', '0', '0', 'monitor:job:list',            'job',        'admin', '2025-05-27 10:47:08', '',      NULL,                  '定时任务菜单'                      ),
       (111,  'druid',                  2,    3,  'druid',               'monitor/druid/index',            '',   '', 1, 0, 'C', '0', '0', 'monitor:druid:list',          'druid',      'admin', '2025-05-27 10:47:08', '',      NULL,                  '数据监控菜单'                      ),
       (112,  'server',                 2,    4,  'server',              'monitor/server/index',           '',   '', 1, 0, 'C', '0', '0', 'monitor:server:list',         'server',     'admin', '2025-05-27 10:47:08', '',      NULL,                  '服务监控菜单'                      ),
       (113,  'cache',                  2,    5,  'cache',               'monitor/cache/index',            '',   '', 1, 0, 'C', '0', '0', 'monitor:cache:list',          'redis',      'admin', '2025-05-27 10:47:08', '',      NULL,                  '缓存监控菜单'                      ),
       (114,  'cache_list',             2,    6,  'cacheList',           'monitor/cache/list',             '',   '', 1, 0, 'C', '0', '0', 'monitor:cache:list',          'redis-list', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '缓存列表菜单'                      ),
       (115,  'build',                  3,    1,  'build',               'tool/build/index',               '',   '', 1, 0, 'C', '0', '0', 'tool:build:list',             'build',      'admin', '2025-05-27 10:47:08', '',      NULL,                  '表单构建菜单'                      ),
       (116,  'gen',                    3,    2,  'gen',                 'tool/gen/index',                 '',   '', 1, 0, 'C', '0', '0', 'tool:gen:list',               'code',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '代码生成菜单'                      ),
       (117,  'swagger',                3,    3,  'swagger',             'tool/swagger/index',             '',   '', 1, 0, 'C', '0', '0', 'tool:swagger:list',           'swagger',    'admin', '2025-05-27 10:47:08', '',      NULL,                  '系统接口菜单'                      ),
       (500,  'operlog',                108,  1,  'operlog',             'monitor/operlog/index',          '',   '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',        'form',       'admin', '2025-05-27 10:47:08', '',      NULL,                  '操作日志菜单'                      ),
       (501,  'logininfor',             108,  2,  'logininfor',          'monitor/logininfor/index',       '',   '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list',     'logininfor', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '登录日志菜单'                      ),
       (1000, 'user_query',             100,  1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1001, 'user_add',               100,  2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1002, 'user_edit',              100,  3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1003, 'user_del',               100,  4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1004, 'user_export',            100,  5,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:export',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1005, 'user_import',            100,  6,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:import',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1006, 'user_resetPwd',          100,  7,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',        '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1007, 'role_query',             101,  1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:role:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1008, 'role_add',               101,  2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:role:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1009, 'role_edit',              101,  3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:role:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1010, 'role_del',               101,  4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:role:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1011, 'role_export',            101,  5,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:role:export',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1012, 'menu_query',             102,  1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:menu:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1013, 'menu_add',               102,  2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:menu:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1014, 'menu_edit',              102,  3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:menu:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1015, 'menu_del',               102,  4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:menu:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1016, 'dept_query',             103,  1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dept:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1017, 'dept_add',               103,  2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dept:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1018, 'dept_edit',              103,  3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dept:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1019, 'dept_del',               103,  4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dept:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1020, 'post_query',             104,  1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:post:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1021, 'post_add',               104,  2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:post:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1022, 'post_edit',              104,  3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:post:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1023, 'post_del',               104,  4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:post:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1024, 'post_export',            104,  5,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'system:post:export',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1025, 'dict_query',             105,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dict:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1026, 'dict_add',               105,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dict:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1027, 'dict_edit',              105,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dict:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1028, 'dict_del',               105,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dict:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1029, 'dict_export',            105,  5,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:dict:export',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1030, 'config_query',           106,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:config:query',         '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1031, 'config_add',             106,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:config:add',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1032, 'config_edit',            106,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:config:edit',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1033, 'config_del',             106,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:config:remove',        '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1034, 'config_export',          106,  5,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:config:export',        '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1035, 'notice_query',           107,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:notice:query',         '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1036, 'notice_add',             107,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:notice:add',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1037, 'notice_edit',            107,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:notice:edit',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1038, 'notice_del',             107,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'system:notice:remove',        '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1039, 'operlog_query',          500,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',       '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1040, 'operlog_del',            500,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',      '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1041, 'operlog_export',         500,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',      '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1042, 'logininfor_query',       501,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',    '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1043, 'logininfor_del',         501,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',   '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1044, 'logininfor_export',      501,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',   '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1045, 'logininfor_unlock',      501,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',   '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1046, 'online_query',           109,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:online:query',        '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1047, 'online_batchLogout',     109,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout',  '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1048, 'online_forceLogout',     109,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout',  '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1049, 'job_query',              110,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:query',           '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1050, 'job_add',                110,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:add',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1051, 'job_edit',               110,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:edit',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1052, 'job_del',                110,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:remove',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1053, 'job_changeStatus',       110,  5,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',    '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1054, 'job_export',             110,  6,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'monitor:job:export',          '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1055, 'gen_query',              116,  1,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:query',              '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1056, 'gen_edit',               116,  2,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:edit',               '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1057, 'gen_remove',             116,  3,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:remove',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1058, 'gen_import',             116,  4,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:import',             '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1059, 'gen_preview',            116,  5,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:preview',            '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (1060, 'gen_code',               116,  6,  '#',                   '',                               '',   '', 1, 0, 'F', '0', '0', 'tool:gen:code',               '#',          'admin', '2025-05-27 10:47:08', '',      NULL,                  ''                                  ),
       (2000, 'mall',                   0,    2,  'mall',                NULL,                             NULL, '', 1, 0, 'M', '1', '0', '',                            'shopping',   'admin', '2025-03-10 07:48:04', 'admin', '2025-06-11 18:44:58', ''                                  ),
       (2001, 'Customers',              0,    0,  'member',              'mall/member/index',              NULL, '', 1, 0, 'C', '0', '0', 'mall:member:list',            'peoples',    'admin', '2025-03-10 07:49:21', 'admin', '2025-06-11 18:42:21', ''                                  ),
       (2002, 'order',                  2000, 5,  'order',               'mall/order/index',               NULL, '', 1, 0, 'C', '1', '0', 'mall:order:list',             'form',       'admin', '2025-03-10 07:50:08', 'admin', '2025-06-11 22:12:13', ''                                  ),
       (2003, 'Products-old',           0,    0,  'product2',            'mall/product2/index',            NULL, '', 1, 0, 'C', '1', '0', 'mall:product2:list',          'phone',      'admin', '2025-03-10 07:52:06', 'admin', '2025-06-23 22:30:39', ''                                  ),
       (2004, 'Gift Cards',             0,    3,  'coupon',              'mall/coupon/index',              NULL, '', 1, 0, 'C', '0', '0', 'mall:coupon:list',            'rate',       'admin', '2025-03-10 07:54:33', 'admin', '2025-06-11 18:39:17', ''                                  ),
       (2006, 'Discounts',              0,    1,  'benefit',             'mall/benefit/index',             '',   '', 1, 0, 'C', '0', '0', 'mall:benefit:list',           'money',      'admin', '2025-05-15 06:51:54', 'admin', '2025-06-11 18:41:10', ''                                  ),
       (2008, 'vorder',                 0,    0,  'mall/vorder',         'mall/vorder/index',              NULL, '', 1, 0, 'M', '1', '0', 'mall:vorder:list',            'build',      'admin', '2025-06-03 11:50:58', 'admin', '2025-06-11 16:05:09', ''                                  ),
       (2009, 'Orders',                 0,    0,  'vorder',              'mall/vorder/index',              NULL, '', 1, 0, 'C', '0', '0', 'mall:vorder:list',            'chart',      'admin', '2025-06-03 11:52:55', 'admin', '2025-06-11 18:43:16', ''                                  ),
       (2013, 'productitem',            2015, 5,  'product/productitem', 'mall/product/productitem/index', NULL, '', 1, 0, 'C', '1', '0', 'mall:product:list',           'button',     'admin', '2025-06-11 12:38:00', 'admin', '2025-06-23 21:50:43', ''                                  ),
       (2014, 'Detail',                 2015, 3,  'vorder/detail',       'mall/vorder/detail/index',       NULL, '', 1, 0, 'C', '0', '0', 'mail:vorder:detail',          'date-range', 'admin', '2025-06-11 15:13:55', 'admin', '2025-06-11 22:13:53', ''                                  ),
       (2015, 'hide',                   0,    0,  'hide',                NULL,                             NULL, '', 1, 0, 'M', '1', '0', NULL,                          'color',      'admin', '2025-06-11 21:49:26', '',      NULL,                  ''                                  ),
       (2016, 'Products',               0,    1,  'product',             'mall/product/index',             NULL, '', 1, 0, 'C', '0', '0', 'mall:product:list',           'example',    'admin', '2025-06-23 15:26:18', 'admin', '2025-06-23 22:30:48', ''                                  ),
       (2020, 'Product Management',     0,    1,  'mall/product',        'mall/product/index',             NULL, '', 1, 0, 'C', '1', '0', 'mall:product:list',           'shopping',   'admin', '2025-06-23 17:58:54', '',      NULL,                  'Product Management Menu'           ),
       (2021, 'product_query',          2020, 1,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'mall:product:query',          '#',          'admin', '2025-06-23 17:58:54', '',      NULL,                  ''                                  ),
       (2022, 'product_add',            2020, 2,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'mall:product:add',            '#',          'admin', '2025-06-23 17:58:54', '',      NULL,                  ''                                  ),
       (2023, 'product_edit',           2020, 3,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'mall:product:edit',           '#',          'admin', '2025-06-23 17:58:54', '',      NULL,                  ''                                  ),
       (2024, 'product_del',            2020, 4,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'mall:product:remove',         '#',          'admin', '2025-06-23 17:58:54', '',      NULL,                  ''                                  ),
       (2025, 'product_export',         2020, 5,  '',                    '',                               '',   '', 1, 0, 'F', '0', '0', 'mall:product:export',         '#',          'admin', '2025-06-23 17:58:54', '',      NULL,                  ''                                  ),
       (2026, 'neworder',               2015, 1,  'vorder/neworder',     'mall/vorder/neworder/index',     NULL, '', 1, 0, 'C', '0', '0', 'mall:order:list',             'button',     'admin', '2025-06-23 23:50:44', '',      NULL,                  ''                                  ),
       (2027, 'mdetail',                2015, 6,  'member/mdetail',      'mall/member/mdetail/index',      NULL, '', 1, 0, 'C', '0', '0', 'mall:member:detail',          'button',     'admin', '2025-06-23 23:56:12', '',      NULL,                  ''                                  ),
       (2028, 'page',                   0,    0,  'page',                'mall/page/index',                NULL, '', 1, 0, 'C', '0', '0', 'mall:page:list',              'build',      'admin', '2025-07-13 17:53:49', '',      NULL,                  ''                                  ),
       (2029, 'home',                   2015, 8,  'page/home',           'mall/page/home/index',           NULL, '', 1, 0, 'C', '0', '0', 'mall:page:list',              'button',     'admin', '2025-07-13 18:14:05', 'admin', '2025-07-13 18:14:18', ''                                  ),
       (2030, 'footer',                 2015, 9,  'page/footer',         'mall/page/footer/index',         NULL, '', 1, 0, 'C', '0', '0', 'mall:page:list',              'button',     'admin', '2025-07-13 18:14:05', 'admin', '2025-07-13 18:14:18', ''                                  ),
       (2031, 'profile',                2015, 10, 'page/profile',        'mall/page/profile/index',        NULL, '', 1, 0, 'C', '0', '0', 'mall:page:list',              'button',     'admin', '2025-07-13 18:14:05', 'admin', '2025-07-13 18:14:18', ''                                  ),
       (2032, 'PGP',                    1,    8,  'pgpKey',              'mall/pgpKey/index',              NULL, '', 1, 0, 'C', '0', '0', 'mall:pgpkey:list',            'build',      'admin', '2025-07-18 21:58:50', '',      NULL,                  ''                                  ),
       (2033, 'Telegram Home Config',   0,    4,  'tgpage/home',         'mall/tgpage/home/index',         NULL, '', 1, 0, 'C', '0', '0', 'mall:tg-home-config:list',    'message',    'admin', '2025-07-28 15:29:51', '',      NULL,                  'Telegram Home Config Menu'         ),
       (2034, 'tg-home-config_query',   2033, 1,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-home-config:query',   '#',          'admin', '2025-07-28 15:29:51', '',      NULL,                  ''                                  ),
       (2035, 'tg-home-config_add',     2033, 2,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-home-config:add',     '#',          'admin', '2025-07-28 15:29:51', '',      NULL,                  ''                                  ),
       (2036, 'tg-home-config_edit',    2033, 3,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-home-config:edit',    '#',          'admin', '2025-07-28 15:29:51', '',      NULL,                  ''                                  ),
       (2037, 'Telegram Policy Config', 0,    5,  'tgpage/policy',       'mall/tgpage/policy/index',       NULL, '', 1, 0, 'C', '0', '0', 'mall:tg-policy-config:list',  'document',   'admin', '2025-07-28 17:33:49', '',      NULL,                  'Telegram Policy Configuration Menu'),
       (2038, 'tg-policy-config_query', 2037, 1,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-policy-config:query', '#',          'admin', '2025-07-28 17:33:49', '',      NULL,                  ''                                  ),
       (2039, 'tg-policy-config_add',   2037, 2,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-policy-config:add',   '#',          'admin', '2025-07-28 17:33:49', '',      NULL,                  ''                                  ),
       (2040, 'tg-policy-config_edit',  2037, 3,  '',                    '',                               NULL, '', 1, 0, 'F', '0', '0', 'mall:tg-policy-config:edit',  '#',          'admin', '2025-07-28 17:33:49', '',      NULL,                  ''                                  ),
       (2041, 'edit',                   2002, 0,  '',                    NULL,                             NULL, '', 1, 0, 'F', '0', '0', 'mall:order:edit',             '#',          'admin', '2025-08-13 11:59:07', '',      NULL,                  ''                                  );



CREATE TABLE `sys_notice`
(
    `notice_id`      int                                                          NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `notice_title`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
    `notice_type`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NOT NULL COMMENT '公告类型（1通知 2公告）',
    `notice_content` longblob COMMENT '公告内容',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
    `create_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`    datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`    datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`notice_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='通知公告表';



CREATE TABLE `sys_oper_log`
(
    `oper_id`        bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '模块标题',
    `business_type`  int                                                            DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
    `method`         varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '方法名称',
    `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '请求方式',
    `operator_type`  int                                                            DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
    `oper_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '操作人员',
    `dept_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '' COMMENT '部门名称',
    `oper_url`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '请求URL',
    `oper_ip`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '主机地址',
    `oper_location`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '操作地点',
    `oper_param`     varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求参数',
    `json_result`    varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '返回参数',
    `status`         int                                                            DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
    `error_msg`      varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误消息',
    `oper_time`      datetime                                                       DEFAULT NULL COMMENT '操作时间',
    `cost_time`      bigint                                                         DEFAULT '0' COMMENT '消耗时间',
    PRIMARY KEY (`oper_id`),
    KEY              `idx_sys_oper_log_bt` (`business_type`),
    KEY              `idx_sys_oper_log_s` (`status`),
    KEY              `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 381
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='操作日志记录';


CREATE TABLE `sys_post`
(
    `post_id`     bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    `post_code`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
    `post_name`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
    `post_sort`   int                                                          NOT NULL COMMENT '显示顺序',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NOT NULL COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`post_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='岗位信息表';


INSERT INTO `sys_post`
VALUES (1, 'ceo', 'ceo', 1, '0', 'admin', '2025-05-27 10:47:08', '', NULL, '');



CREATE TABLE `sys_role`
(
    `role_id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色名称',
    `role_key`            varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
    `role_sort`           int                                                           NOT NULL COMMENT '显示顺序',
    `data_scope`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    `menu_check_strictly` tinyint(1)                                                    DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
    `dept_check_strictly` tinyint(1)                                                    DEFAULT '1' COMMENT '部门树选择项是否关联显示',
    `status`              char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NOT NULL COMMENT '角色状态（0正常 1停用）',
    `del_flag`            char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time`         datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time`         datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`              varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`role_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 104
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色信息表';

INSERT INTO `sys_role`
VALUES (1,   '超级管理员', 'admin',      1, '1', 1, 1, '0', '0', 'admin', '2025-05-27 10:47:08', '',      NULL,                  '超级管理员'),
       (101, 'manager',    'manager',    0, '4', 1, 1, '0', '0', 'admin', '2025-08-13 11:35:53', 'admin', '2025-08-13 12:00:16', NULL        ),
       (102, 'accountant', 'accountant', 0, '4', 1, 1, '0', '0', 'admin', '2025-08-13 11:45:33', 'admin', '2025-08-13 12:03:30', NULL        ),
       (103, 'operator',   'operator',   0, '4', 1, 1, '0', '0', 'admin', '2025-08-13 11:48:47', 'admin', '2025-08-13 12:07:34', NULL        );



CREATE TABLE `sys_role_dept`
(
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `dept_id` bigint NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`, `dept_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色和部门关联表';



CREATE TABLE `sys_role_menu`
(
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `menu_id` bigint NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色和菜单关联表';
INSERT INTO `sys_role_menu`
VALUES (1,   2020),
       (1,   2021),
       (1,   2022),
       (1,   2023),
       (1,   2024),
       (1,   2025),
       (101, 2   ),
       (101, 3   ),
       (101, 109 ),
       (101, 110 ),
       (101, 111 ),
       (101, 112 ),
       (101, 113 ),
       (101, 114 ),
       (101, 115 ),
       (101, 116 ),
       (101, 117 ),
       (101, 1046),
       (101, 1047),
       (101, 1048),
       (101, 1049),
       (101, 1050),
       (101, 1051),
       (101, 1052),
       (101, 1053),
       (101, 1054),
       (101, 1055),
       (101, 1056),
       (101, 1057),
       (101, 1058),
       (101, 1059),
       (101, 1060),
       (101, 2000),
       (101, 2001),
       (101, 2002),
       (101, 2004),
       (101, 2006),
       (101, 2008),
       (101, 2009),
       (101, 2013),
       (101, 2014),
       (101, 2015),
       (101, 2016),
       (101, 2020),
       (101, 2021),
       (101, 2022),
       (101, 2023),
       (101, 2024),
       (101, 2025),
       (101, 2026),
       (101, 2027),
       (101, 2028),
       (101, 2029),
       (101, 2030),
       (101, 2031),
       (101, 2033),
       (101, 2034),
       (101, 2035),
       (101, 2036),
       (101, 2037),
       (101, 2038),
       (101, 2039),
       (101, 2040),
       (101, 2041),
       (102, 2000),
       (102, 2002),
       (102, 2008),
       (102, 2009),
       (102, 2013),
       (102, 2014),
       (102, 2015),
       (102, 2026),
       (102, 2027),
       (102, 2029),
       (102, 2030),
       (102, 2031),
       (102, 2041),
       (103, 2000),
       (103, 2002),
       (103, 2008),
       (103, 2009),
       (103, 2014),
       (103, 2015),
       (103, 2026),
       (103, 2027),
       (103, 2041);



CREATE TABLE `sys_user`
(
    `user_id`     bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `dept_id`     bigint                                                        DEFAULT NULL COMMENT '部门ID',
    `user_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
    `nick_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
    `user_type`   varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   DEFAULT '00' COMMENT '用户类型（00系统用户）',
    `email`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '用户邮箱',
    `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '手机号码',
    `sex`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
    `avatar`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像地址',
    `password`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    `del_flag`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `login_ip`    varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最后登录IP',
    `login_date`  datetime                                                      DEFAULT NULL COMMENT '最后登录时间',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 104
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户信息表';

INSERT INTO `sys_user`
VALUES (1,   100, 'admin',         'admin',         '00', 'admin@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '172.25.0.1', '2025-08-16 14:29:32', 'admin', '2025-05-27 10:47:08', '',      '2025-08-16 06:29:32', '管理员'),
       (101, 101, 'manager',       'manager',       '00', '',              '',            '0', '', '$2a$10$Sfl.Hpy5zmlmrYDbvlffe.xirnHhzBGGM/dfzss19yjJwuIcQSUXC', '0', '0', '172.25.0.1', '2025-08-13 20:00:24', 'admin', '2025-08-13 11:34:05', 'admin', '2025-08-13 12:00:23', NULL    ),
       (102, 102, 'accountant_01', 'accountant_01', '00', '',              '',            '0', '', '$2a$10$awRYWgsrUNnIFhco8aXcF.CO.z1W4PXDfFWuqSSGRxNf.OQss3n6S', '0', '0', '172.25.0.1', '2025-08-13 20:02:23', 'admin', '2025-08-13 11:46:26', '',      '2025-08-13 12:02:22', NULL    ),
       (103, 103, 'operator_01',   'operator_01',   '00', '',              '',            '0', '', '$2a$10$ylMHDXkBe/sxX41LPK5li.Sfngq8ReVF6aH4auFdx8P7mDs7y817W', '0', '0', '172.25.0.1', '2025-08-13 20:08:59', 'admin', '2025-08-13 11:52:34', '',      '2025-08-13 12:08:59', NULL    );



CREATE TABLE `sys_user_post`
(
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '岗位ID',
    PRIMARY KEY (`user_id`, `post_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户与岗位关联表';

INSERT INTO `sys_user_post`
VALUES (1, 1);



CREATE TABLE `sys_user_role`
(
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户和角色关联表';

INSERT INTO `sys_user_role`
VALUES (1,   1  ),
       (101, 101),
       (102, 102),
       (103, 103);



CREATE TABLE `tg_home_config`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `banner_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `title`        varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `description`  text COLLATE utf8mb4_general_ci,
    `buttons`      json                                    DEFAULT NULL,
    `create_time`  datetime                                DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`    varchar(64) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `remark`       varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='Telegram首页配置表';

INSERT INTO `tg_home_config`
VALUES (1, '/profile/upload/2025/08/12/acat_20250812051445A004.jpeg', 'Welcome to Our Telegram Store', '\nWe are a dedicated provider of a variety of fruits. Telegram is our main shopping platform. However, we also have a web-based shop for better visibility and control:\n🌐 https://www.fruitshop.com\n\nWe also strongly encourage you to join our Telegram channel to stay updated with special events and our most recent collections:\n📢 https://t.me/fruitshop\n\nLastly, please read and acknowledge our *Refund/Reship Policy* carefully. Placing an order on our shop means you agree to our policy.', '[
  {
    \
  "label\": \"📦 Fruit Shop Policy\",
  \
  "action\": \"view_policy\"
  },
  {
    \
  "label\": \"👤Profile\",
  \
  "action\": \"view_profile\"
  },
  {
    \
  "label\": \"🛍 Online Store\",
  \
  "action\": \"enter_shop\"
  },
  {
    \
  "label\": \"🛍 Local Trade\",
  \
  "action\": \"enter_local_trade\"
  },
  {
    \
  "label\": \"🛒 My Cart\",
  \
  "action\": \"view_cart\"
  },
  {
    \
  "label\": \"📦 Order History\",
  \
  "action\": \"view_orders\"
  }
]', '2025-07-28 15:25:07', '2025-08-12 05:14:49', 'admin', '默认Telegram首页配置');



CREATE TABLE `tg_policy_config`
(
    `id`               bigint NOT NULL AUTO_INCREMENT,
    `privacy_policy`   text COLLATE utf8mb4_general_ci,
    `terms_of_service` text COLLATE utf8mb4_general_ci,
    `refund_policy`    text COLLATE utf8mb4_general_ci,
    `shipping_policy`  text COLLATE utf8mb4_general_ci,
    `contact_info`     text COLLATE utf8mb4_general_ci,
    `create_time`      datetime                                DEFAULT CURRENT_TIMESTAMP,
    `update_time`      datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`        varchar(64) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `remark`           varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='Telegram Policy配置表';

INSERT INTO `tg_policy_config`
VALUES (1, 'We respect your privacy and are committed to protecting your personal data. This privacy policy explains how we collect, use, and safeguard your information when you use our services.', ' By using our services, you agree to these terms and conditions. Please read them carefully before making any purchases.', 'We offer refunds within 7 days of purchase for digital products. Physical products may be returned within 14 days in original condition.', 'We ship worldwide with discreet packaging. Shipping times vary by location, typically 7-21 business days.', 'For support, please contact us at support@yourdomain.com or through our Telegram bot.', '2025-07-28 17:42:09', '2025-07-28 17:42:56', 'admin', 'Default Telegram Policy Configuration');


