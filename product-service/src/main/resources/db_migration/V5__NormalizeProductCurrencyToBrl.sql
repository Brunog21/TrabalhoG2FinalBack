UPDATE tb_product
SET currency = 'BRL'
WHERE currency IS NULL OR UPPER(currency) <> 'BRL';
