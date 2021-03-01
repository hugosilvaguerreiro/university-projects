#########################################
##########  Helper transducers   #######
#########################################

#COMPILE dd2dd
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/dd2dd.txt | fstarcsort > helper_transducers/dd2dd.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/dd2dd.fst | dot -Tpdf  > helper_transducers/dd2dd.pdf

#COMPILE dd2texto
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/dd2texto.txt | fstarcsort > helper_transducers/dd2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/dd2texto.fst | dot -Tpdf  > helper_transducers/dd2texto.pdf

#COMPILE slash
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/slash.txt | fstarcsort > helper_transducers/slash.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/slash.fst | dot -Tpdf  > helper_transducers/slash.pdf

#COMPILE aaaa2aaaa
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/aaaa2aaaa.txt | fstarcsort > helper_transducers/aaaa2aaaa.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/aaaa2aaaa.fst | dot -Tpdf  > helper_transducers/aaaa2aaaa.pdf

#COMPILE enm2ptm
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/enm2ptm.txt | fstarcsort > helper_transducers/enm2ptm.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/enm2ptm.fst | dot -Tpdf  > helper_transducers/enm2ptm.pdf

#COMPILE 20002doismil
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/20002doismil.txt | fstarcsort > helper_transducers/20002doismil.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/20002doismil.fst | dot -Tpdf  > helper_transducers/20002doismil.pdf

#COMPILE slash2de
fstcompile --isymbols=syms.txt --osymbols=syms.txt  helper_transducers/slash2de.txt | fstarcsort > helper_transducers/slash2de.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait helper_transducers/slash2de.fst | dot -Tpdf  > helper_transducers/slash2de.pdf
######################################################################################################

#######################################
#############     EX a 1   ############
#######################################

#COMPILE mmm2mm
fstcompile --isymbols=syms.txt --osymbols=syms.txt  mmm2mm.txt | fstarcsort > mmm2mm.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait mmm2mm.fst | dot -Tpdf  > mmm2mm.pdf


#######################################
#############     EX a 2   ############
#######################################

#CREATE (CONCAT) misto2numerico
fstconcat helper_transducers/dd2dd.fst helper_transducers/slash.fst > dds.fst
fstconcat dds.fst mmm2mm.fst > ddsmm.fst
fstconcat ddsmm.fst helper_transducers/slash.fst > ddsmms.fst
fstconcat ddsmms.fst helper_transducers/aaaa2aaaa.fst > misto2numerico.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait misto2numerico.fst | dot -Tpdf  > misto2numerico.pdf
rm dds.fst ddsmm.fst ddsmms.fst


#######################################
#############     EX b 3   ############
#######################################

#CREATE (CONCAT) en2pt
fstconcat helper_transducers/dd2dd.fst helper_transducers/slash.fst > dds.fst
fstconcat dds.fst helper_transducers/enm2ptm.fst > ddsmm.fst
fstconcat ddsmm.fst helper_transducers/slash.fst > ddsmms.fst
fstconcat ddsmms.fst helper_transducers/aaaa2aaaa.fst > en2pt.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait en2pt.fst | dot -Tpdf  > en2pt.pdf
rm dds.fst ddsmm.fst ddsmms.fst

#######################################
#############     EX b 4   ############
#######################################

#CREATE (INVERT) pt2en
fstinvert en2pt.fst > pt2en.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait pt2en.fst | dot -Tpdf  > pt2en.pdf


#######################################
#############     EX c 5   ############
#######################################

#CREATE (COMPOSE) dia
fstcompose helper_transducers/dd2dd.fst helper_transducers/dd2texto.fst > diatemp.fst
fstminimize diatemp.fst > dia.fst
fstdraw --isymbols=syms.txt --osymbols=syms.txt --portrait dia.fst | dot -Tpdf  > dia.pdf
rm diatemp.fst



#######################################
#############     EX c 6   ############
#######################################

#COMPILE mes
fstcompile --isymbols=syms.txt --osymbols=syms.txt  mes.txt | fstarcsort > mes.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait mes.fst | dot -Tpdf  > mes.pdf



#######################################
#############     EX c 7   ############
#######################################

#CREATE (CONCAT) ano
fstconcat helper_transducers/20002doismil.fst helper_transducers/dd2texto.fst > ano.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait ano.fst | dot -Tpdf  > ano.pdf


#######################################
#############     EX c 8   ############
#######################################

#CREATE (CONCAT) numerico2texto
fstconcat dia.fst helper_transducers/slash2de.fst > dds.fst
fstconcat dds.fst mes.fst > ddsmm.fst
fstconcat ddsmm.fst helper_transducers/slash2de.fst > ddsmms.fst
fstconcat ddsmms.fst ano.fst > numerico2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait numerico2texto.fst | dot -Tpdf  > numerico2texto.pdf
rm dds.fst ddsmm.fst ddsmms.fst

#######################################
#############     EX d 9   ############
#######################################

#CREATE (COMPOSE) misto2texto
fstcompose misto2numerico.fst numerico2texto.fst > misto2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait misto2texto.fst | dot -Tpdf  > misto2texto.pdf

#######################################
#############     EX d 10   ###########
#######################################

#CREATE (UNION) data2texto
fstunion misto2texto.fst numerico2texto.fst > data2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait data2texto.fst | dot -Tpdf  > data2texto.pdf


#######################################
########     Birthday Dates   #########
#######################################
fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83475_misto.txt | fstarcsort > dates/83475_misto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_misto.fst | dot -Tpdf  > dates/83475_misto.pdf

fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83475_pt.txt | fstarcsort > dates/83475_pt.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_pt.fst | dot -Tpdf  > dates/83475_pt.pdf

fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83475_numerico.txt | fstarcsort > dates/83475_numerico.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_numerico.fst | dot -Tpdf  > dates/83475_numerico.pdf

fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83526_misto.txt | fstarcsort > dates/83526_misto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_misto.fst | dot -Tpdf  > dates/83526_misto.pdf

fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83526_pt.txt | fstarcsort > dates/83526_pt.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_pt.fst | dot -Tpdf  > dates/83526_pt.pdf

fstcompile --isymbols=syms.txt --osymbols=syms.txt  dates/83526_numerico.txt | fstarcsort > dates/83526_numerico.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_numerico.fst | dot -Tpdf  > dates/83526_numerico.pdf

fstcompose dates/83475_misto.fst misto2numerico.fst > dates/83475_misto2numerico.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_misto2numerico.fst | dot -Tpdf  > dates/83475_misto2numerico.pdf

fstcompose dates/83475_pt.fst pt2en.fst > dates/83475_pt2en.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_pt2en.fst | dot -Tpdf  > dates/83475_pt2en.pdf

fstcompose dates/83475_numerico.fst numerico2texto.fst > dates/83475_numerico2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_numerico2texto.fst | dot -Tpdf  > dates/83475_numerico2texto.pdf

fstcompose dates/83475_misto.fst misto2texto.fst > dates/83475_misto2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_misto2texto.fst | dot -Tpdf  > dates/83475_misto2texto.pdf

fstcompose dates/83475_numerico.fst data2texto.fst > dates/83475_data2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83475_data2texto.fst | dot -Tpdf  > dates/83475_data2texto.pdf

fstcompose dates/83526_misto.fst misto2numerico.fst > dates/83526_misto2numerico.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_misto2numerico.fst | dot -Tpdf  > dates/83526_misto2numerico.pdf

fstcompose dates/83526_pt.fst pt2en.fst > dates/83526_pt2en.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_pt2en.fst | dot -Tpdf  > dates/83526_pt2en.pdf

fstcompose dates/83526_numerico.fst numerico2texto.fst > dates/83526_numerico2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_numerico2texto.fst | dot -Tpdf  > dates/83526_numerico2texto.pdf

fstcompose dates/83526_misto.fst misto2texto.fst > dates/83526_misto2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_misto2texto.fst | dot -Tpdf  > dates/83526_misto2texto.pdf

fstcompose dates/83526_misto.fst data2texto.fst > dates/83526_data2texto.fst
fstdraw    --isymbols=syms.txt --osymbols=syms.txt --portrait dates/83526_data2texto.fst | dot -Tpdf  > dates/83526_data2texto.pdf
