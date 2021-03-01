TRUNCATE categoria cascade;
TRUNCATE categoria_simples cascade;
TRUNCATE super_categoria cascade;
TRUNCATE constituida cascade;
TRUNCATE produto cascade;
TRUNCATE fornecedor cascade;
TRUNCATE fornece_sec cascade;
TRUNCATE corredor cascade;
TRUNCATE prateleira cascade;
TRUNCATE planograma cascade;
TRUNCATE evento_reposicao cascade;
TRUNCATE reposicao cascade;

insert into categoria values('peixe');
insert into categoria values('carne');
insert into categoria values('fruta');
insert into categoria values('congelados');
insert into categoria values('alimentacao');

insert into categoria values('shampoo');
insert into categoria values('gel de banho');
insert into categoria values('desodorizante');
insert into categoria values('perfume');
insert into categoria values('higiene');

insert into categoria values('vassoura');
insert into categoria values('detergente');
insert into categoria values('lava-tudo');
insert into categoria values('esfregona');
insert into categoria values('menage');

insert into categoria_simples values('shampoo');
insert into categoria_simples values('gel de banho');
insert into categoria_simples values('desodorizante');
insert into categoria_simples values('perfume');
insert into super_categoria values('higiene');

insert into categoria_simples values('vassoura');
insert into categoria_simples values('detergente');
insert into categoria_simples values('lava-tudo');
insert into categoria_simples values('esfregona');
insert into super_categoria values('menage');

insert into categoria_simples values('peixe');
insert into categoria_simples values('carne');
insert into categoria_simples values('fruta');
insert into categoria_simples values('congelados');
insert into super_categoria values('alimentacao');

insert into constituida values('alimentacao', 'peixe');
insert into constituida values('alimentacao', 'carne');
insert into constituida values('alimentacao', 'fruta');
insert into constituida values('alimentacao', 'congelados');

insert into constituida values('higiene', 'shampoo');
insert into constituida values('higiene', 'gel de banho');
insert into constituida values('higiene', 'desodorizante');
insert into constituida values('higiene', 'perfume');

insert into constituida values('menage', 'vassoura');
insert into constituida values('menage', 'detergente');
insert into constituida values('menage', 'lava-tudo');
insert into constituida values('menage', 'esfregona');

insert into fornecedor values('11111111', 'Manuel');
insert into fornecedor values('22222222', 'Joaquim');
insert into fornecedor values('33333333', 'Delfina');
insert into fornecedor values('44444444', 'Zeferina');

insert into produto values(134, 'Robalo', 'peixe', 11111111, '2017-11-04');
insert into produto values(135, 'Sardinha', 'peixe', 22222222, '2017-10-25');
insert into produto values(136, 'Costeleta de Porco', 'carne', 33333333, '2017-11-25');
insert into produto values(137, 'Frango do Campo', 'carne', 44444444, '2017-02-01');
insert into produto values(138, 'Banana da Madeira', 'fruta', 11111111, '2016-08-14');
insert into produto values(139, 'Maracuja', 'fruta', 22222222, '2016-12-21');
insert into produto values(140, 'Camarao Cozido', 'congelados', 33333333, '2016-09-14');
insert into produto values(141, 'Gelado de Baunilha', 'congelados', 44444444, '2017-10-21');

insert into produto values(142, 'Shampoo Aveia', 'shampoo', 11111111, '2016-09-14');
insert into produto values(143, 'Shampoo Frutos do Bosque', 'shampoo', 22222222, '2017-10-21');
insert into produto values(144, 'GB X', 'gel de banho', 33333333, '2016-09-14');
insert into produto values(145, 'GB Y', 'gel de banho', 44444444, '2017-10-21');
insert into produto values(146, 'Deo XPTO', 'desodorizante', 11111111, '2016-09-14');
insert into produto values(147, 'Deod ABCD', 'desodorizante',22222222, '2017-10-21');
insert into produto values(148, 'Perfume Frances', 'perfume', 33333333, '2016-09-14');
insert into produto values(149, 'Perfume de Tras os Montes', 'perfume', 44444444, '2017-10-21');

insert into produto values(150, 'Vassoura azul', 'vassoura', 11111111, '2016-09-14');
insert into produto values(151, 'Vassoura verde', 'vassoura', 22222222, '2017-10-21');
insert into produto values(152, 'Det ABQ Crystal Blue', 'detergente', 33333333, '2016-09-14');
insert into produto values(153, 'Det Neo Soft Stuff', 'detergente', 44444444, '2017-10-21');
insert into produto values(154, 'LT A amoniacal', 'lava-tudo',11111111, '2016-09-14');
insert into produto values(155, 'LT B nao-amoniacal', 'lava-tudo', 22222222, '2017-10-21');
insert into produto values(156, 'Esfregona Top Market', 'esfregona', 33333333, '2016-09-14');
insert into produto values(157, 'Esfregona do neolitico', 'esfregona', 44444444, '2017-10-21');

insert into fornece_sec values('22222222', 134);
insert into fornece_sec values('33333333', 135);
insert into fornece_sec values('44444444', 136);
insert into fornece_sec values('11111111', 137);
insert into fornece_sec values('22222222', 138);
insert into fornece_sec values('33333333', 139);
insert into fornece_sec values('44444444', 140);
insert into fornece_sec values('11111111', 141);
insert into fornece_sec values('33333333', 141);

insert into fornece_sec values('22222222', 142);
insert into fornece_sec values('33333333', 143);
insert into fornece_sec values('44444444', 144);
insert into fornece_sec values('11111111', 145);
insert into fornece_sec values('22222222', 146);
insert into fornece_sec values('33333333', 147);
insert into fornece_sec values('44444444', 148);
insert into fornece_sec values('11111111', 149);

insert into fornece_sec values('22222222', 150);
insert into fornece_sec values('33333333', 151);
insert into fornece_sec values('44444444', 152);
insert into fornece_sec values('11111111', 153);
insert into fornece_sec values('22222222', 154);
insert into fornece_sec values('33333333', 155);
insert into fornece_sec values('44444444', 156);
insert into fornece_sec values('11111111', 157);


insert into corredor values(1, 4.50);
insert into corredor values(2, 4.00);
insert into corredor values(3, 4.25);

insert into prateleira values(1, 'esquerdo', 1.80);
insert into prateleira values(1, 'esquerdo', 1.00);
insert into prateleira values(1, 'direito', 1.80);
insert into prateleira values(1, 'direito', 1.00);
insert into prateleira values(2, 'esquerdo', 1.50);
insert into prateleira values(2, 'direito', 1.50);
insert into prateleira values(3, 'esquerdo', 1.80);
insert into prateleira values(3, 'esquerdo', 1.40);
insert into prateleira values(3, 'esquerdo', 1.00);
insert into prateleira values(3, 'direito', 1.80);
insert into prateleira values(3, 'direito', 1.40);
insert into prateleira values(3, 'direito', 1.00);

insert into planograma values(134, 3, 'esquerdo', 1.80, 2, 3, 1);
insert into planograma values(135, 3, 'esquerdo', 1.40, 2, 31, 2);
insert into planograma values(136, 3, 'direito', 1.40, 3, 54, 3);
insert into planograma values(137, 3, 'esquerdo', 1.00, 2, 12, 4);
insert into planograma values(138, 3, 'esquerdo', 1.80, 1, 42, 5);
insert into planograma values(139, 3, 'direito', 1.00, 2, 14, 6);
insert into planograma values(140, 3, 'esquerdo', 1.80, 2, 22, 7);
insert into planograma values(141, 3, 'direito', 1.80, 2, 18, 8);

insert into planograma values(142, 1, 'esquerdo', 1.80, 3, 18, 9);
insert into planograma values(143, 1, 'esquerdo', 1.00, 3, 19, 10);
insert into planograma values(144, 1, 'direito', 1.80, 3, 10, 11);
insert into planograma values(145, 1, 'direito', 1.00, 1, 6, 12);
insert into planograma values(146, 1, 'esquerdo', 1.80, 3, 8, 13);
insert into planograma values(147, 1, 'esquerdo', 1.00, 2, 15, 14);
insert into planograma values(148, 1, 'direito', 1.80, 4, 21, 15);
insert into planograma values(149, 1, 'direito', 1.00, 2, 12, 16);

insert into planograma values(150, 2, 'esquerdo', 1.50, 1, 5, 17);
insert into planograma values(151, 2, 'direito', 1.50, 1, 8, 18);
insert into planograma values(152, 2, 'esquerdo', 1.50, 2, 27, 19);
insert into planograma values(153, 2, 'direito', 1.50, 2, 13, 20);
insert into planograma values(154, 2, 'esquerdo', 1.50, 3, 31, 21);
insert into planograma values(155, 2, 'direito', 1.50, 3, 18, 22);
insert into planograma values(156, 2, 'esquerdo', 1.50, 1, 12, 23);
insert into planograma values(157, 2, 'esquerdo', 1.50, 1, 3, 24);

insert into evento_reposicao values('Manel1', '2017-11-11 01:00:00');
insert into evento_reposicao values('Manel2', '2017-11-11 01:00:01');
insert into evento_reposicao values('Manel3', '2017-11-11 01:00:02');
insert into evento_reposicao values('Manel4', '2017-11-11 01:00:04');
insert into evento_reposicao values('Manel5', '2017-11-11 01:00:05');
insert into evento_reposicao values('Manel6', '2017-11-11 01:00:06');
insert into evento_reposicao values('Manel7', '2017-11-11 01:00:07');
insert into evento_reposicao values('Manel8', '2017-11-11 01:00:08');

insert into reposicao values(134, 3, 'esquerdo', 1.80, 'Manel1', '2017-11-11 01:00:00', 2);
insert into reposicao values(135, 3, 'esquerdo', 1.40, 'Manel2', '2017-11-11 01:00:01', 3);
insert into reposicao values(136, 3, 'direito', 1.40, 'Manel3', '2017-11-11 01:00:02', 4);
insert into reposicao values(137, 3, 'esquerdo', 1.00, 'Manel4', '2017-11-11 01:00:04', 1);
insert into reposicao values(138, 3, 'esquerdo', 1.80, 'Manel5', '2017-11-11 01:00:05', 2);
insert into reposicao values(139, 3, 'direito', 1.00, 'Manel6', '2017-11-11 01:00:06', 7);
insert into reposicao values(140, 3, 'esquerdo', 1.80, 'Manel7', '2017-11-11 01:00:07', 3);
insert into reposicao values(141, 3, 'direito', 1.80, 'Manel8', '2017-11-11 01:00:08', 2);
