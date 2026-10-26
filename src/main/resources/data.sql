-- Demo data: HVAC distribution, matching the case study on jamesboggs.online. Not real prices.

INSERT INTO products (sku, name, product_line, list_price, unit_cost) VALUES
  ('SKU-100', 'Air handler, 2-ton',            'Climate',  410.00, 265.00),
  ('SKU-110', 'Condensing unit, 3-ton',        'Climate',  680.00, 430.00),
  ('SKU-200', 'Condenser fan motor',           'Outdoor',  145.00,  92.00),
  ('SKU-210', 'Coil guard',                    'Outdoor',   92.00,  58.00),
  ('SKU-300', 'Programmable thermostat',       'Controls',  58.00,  34.00),
  ('SKU-310', 'Zone control panel',            'Controls', 232.00, 148.00);

INSERT INTO customers (id, name, tier) VALUES
  ('brightline',   'Brightline Supply',      'PREFERRED'),
  ('coastal-air',  'Coastal Air Mechanical', 'KEY'),
  ('palm-comfort', 'Palm Comfort Services',  'STANDARD');
