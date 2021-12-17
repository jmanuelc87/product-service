create table if not exists ecommerce_products
(
    id          bigserial primary key,
    serial_code varchar(50),
    name        text,
    description text,
    price       decimal(10, 2)
);

create table if not exists ecommerce_categories
(
    id          bigserial primary key,
    name        varchar(512),
    description text,
    enabled     boolean
);

create table if not exists ecommerce_attributes
(
    id         bigserial primary key,
    attribute  varchar(512),
    value      varchar(2048),
    product_id bigint,

    foreign key (product_id) references ecommerce_products (id)
);

create table if not exists ecommerce_products_join_categories
(
    product_id  bigint,
    category_id bigint,

    foreign key (product_id) references ecommerce_products (id),
    foreign key (category_id) references ecommerce_categories (id)
);
