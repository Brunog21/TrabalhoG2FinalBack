-- Distribui categorias entre produtos existentes sem category_id (1–8 = categorias do app)
UPDATE tb_product
SET category_id = ((id - 1) % 8) + 1
WHERE category_id IS NULL;
