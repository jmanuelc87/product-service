alter table if exists ecommerce_products
    add column if not exists name_ts tsvector;

alter table if exists ecommerce_attributes
    add column if not exists attribute_value_ts tsvector;

create index ecommerce_products_search_idx on ecommerce_products using gin (name_ts);

create index ecommerce_attributes_search_idx on ecommerce_attributes using gin (attribute_value_ts);


create or replace function update_products_ts_fields()
    returns trigger
    language plpgsql
as
$$
begin
    update ecommerce_products
    set name_ts = to_tsvector(name)
    where id = new.id;

    RETURN new;
end;
$$;


create or replace function update_attributes_ts_fields()
    returns trigger
    language plpgsql
as
$$
begin
    update ecommerce_attributes
    set attribute_value_ts = to_tsvector(coalesce(concat(attribute, ' is ', value), ''))
    where id = new.id;

    RETURN new;
end;
$$;


create trigger trg_update_products_ts_field
    after insert
    on ecommerce_products
    for each row
execute function update_products_ts_fields();

create trigger trg_update_products_ts_field
    after insert
    on ecommerce_attributes
    for each row
execute function update_attributes_ts_fields();
