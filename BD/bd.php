<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>BDSuperMercado</title>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>


<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
   

  </head>

  <body>


<nav class="navbar navbar-default">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <a class="navbar-brand" href="?">BDSuperMercado</a>

    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      <ul class="nav navbar-nav">
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Mostrar tabela <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="?action=consult&table=categoria">categoria</a></li>
            <li><a href="?action=consult&table=categoria_simples">categoria_simples</a></li>
            <li><a href="?action=consult&table=constituida">constituida</a></li>
            <li><a href="?action=consult&table=corredor">corredor</a></li>
            <li><a href="?action=consult&table=evento_reposicao">evento_reposicao</a></li>
            <li><a href="?action=consult&table=fornece_sec">fornece_sec</a></li>
            <li><a href="?action=consult&table=fornecedor">fornecedor</a></li>
            <li><a href="?action=consult&table=planograma">planograma</a></li>
            <li><a href="?action=consult&table=prateleira">prateleira</a></li>
            <li><a href="?action=consult&table=produto">produto</a></li>
             <li><a href="?action=consult&table=reposicao">reposição</a></li>
            <li><a href="?action=consult&table=super_categoria">super_categoria</a></li>
          </ul>
        </li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Inserir <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="#" data-toggle="modal" data-target="#insereSuperCategoria">Super-categoria</a></li>
            <li><a href="#" data-toggle="modal" data-target="#insereCategoriaSimples">Categoria-simples</a></li>
            <li><a href="#" data-toggle="modal" data-target="#insereConstituida">Sub-Categoria</a></li>
            <li><a href="#" data-toggle="modal" data-target="#insereFornecedor">Fornecedor</a></li>
            <li><a href="#" data-toggle="modal" data-target="#insereFornecedorSec">Fornecedor Secundário</a></li>
            <li><a href="#" data-toggle="modal" data-target="#insereProduto">Produto</a></li>
          </ul>
        </li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Consultar <span class="caret"></span></a>
          <ul class="dropdown-menu">
                <li><a href="#" data-toggle="modal" data-target="#searchReposicao">Reposição por ean</a></li>
                <li><a href="#" data-toggle="modal" data-target="#getSubCategorias">Subcategorias de uma categoria</a></li> 
          </ul>
        </li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Alterar <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="?action=consult&table=alterDesign">Alterar designação do produto</a></li>
          </ul>
        </li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Remover <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="?action=consult&table=removeCategoria">Categoria</a></li>
            <li><a href="?action=consult&table=removeFornecedor">Fornecedor</a></li>
            <li><a href="?action=consult&table=removeConstituida">Sub-Categoria</a></li>
            <li><a href="?action=consult&table=removeProduto">Produto</a></li>
          </ul>
        </li>
        
      </ul>
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>
    <?php
    try
    {
        $host = "db.ist.utl.pt";
        $user ="ist424752";
        $password = "2336589";
        $dbname = $user;
    
        $db = new PDO("pgsql:host=$host;dbname=$dbname", $user, $password);
        $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        switch ($_GET["action"]) {
            case "consult":
                echo 'A consultar a tabela ' . $_GET["table"] . '.';
                switch ($_GET["table"]) {
                    case "categoria":
                        $sql = "SELECT * FROM categoria;";
                        print_table(array('nome'), $db->query($sql));
                        break;
                    case "categoria_simples":
                        $sql = "SELECT * FROM categoria_simples;";
                        print_table(array('nome'), $db->query($sql));
                        break;  
                    case "constituida":
                        $sql = "SELECT * FROM constituida;";
                        print_table(array('super_categoria', 'categoria'), $db->query($sql));
                        break;
                    case "corredor":
                        $sql = "SELECT * FROM corredor;";
                        print_table(array('nro', 'largura'), $db->query($sql));
                        break;
                    case "evento_reposicao":
                        $sql = "SELECT * FROM evento_reposicao;";
                        print_table(array('operador', 'instante'), $db->query($sql));
                        break;
                    case "fornece_sec":
                        $sql = "SELECT * FROM fornece_sec;";
                        print_table(array('nif', 'ean'), $db->query($sql));
                        break;
                    case "fornecedor":
                        $sql = "SELECT * FROM fornecedor;";
                        print_table(array('nif', 'nome'), $db->query($sql));
                        break;
                    case "planograma":
                        $sql = "SELECT * FROM planograma;";
                        print_table(array('ean', 'nro', 'lado', 'altura', 'face', 'unidades', 'loc'), $db->query($sql));
                        break;
                    case "prateleira":
                        $sql = "SELECT * FROM prateleira;";
                        print_table(array('nro', 'lado', 'altura'), $db->query($sql));
                        break;
                    case "produto":
                        $sql = "SELECT * FROM produto;";
                        print_table(array('ean', 'design', 'categoria', 'forn_primario', 'data'), $db->query($sql));
                        break;
                    case "reposicao":
                        $sql = "SELECT * FROM reposicao;";
                        print_table(array('ean', 'nro', 'lado', 'altura', 'operador', 'instante', 'unidades'), $db->query($sql));
                        break;
                    case "super_categoria":
                        $sql = "SELECT * FROM super_categoria;";
                        print_table(array('nome'), $db->query($sql));
                        break;
                    case "removeCategoria":
                        $sql = "SELECT * FROM categoria;";
                        delete_table(array('nome'), $db->query($sql), 'categoria');
                        break;
                    case "removeFornecedor":
                        $sql = "SELECT * FROM fornecedor;";
                        delete_table(array('nif', 'nome'), $db->query($sql), 'fornecedor');
                        break;
                    case "removeProduto":
                        $sql = "SELECT * FROM produto;";
                        delete_table(array('ean', 'design', 'categoria', 'forn_primario', 'data'), $db->query($sql), 'produto');
                        break;
                    case "removeConstituida":
                        $sql = "SELECT * FROM constituida;";
                        delete_table(array('super_categoria', 'categoria'), $db->query($sql), 'constituida');
                        break;
                    case "alterDesign":
                        $sql = "SELECT * FROM produto ORDER BY ean;";
                        alter_design(array('ean', 'design', 'categoria', 'forn_primario', 'data'), $db->query($sql));
                        break;
                }
                break;
            case "specialConsult":
                switch ($_GET["table"]) {
                    case 'reposicao':
                        $sth = $db->prepare("SELECT ean, operador, instante, unidades FROM reposicao WHERE ean=? ;");
                        $sth->execute(array(intval($_GET["ean"])));
                        $result = $sth->fetchAll();
                        print_table(array('ean', 'operador', 'instante', 'unidades'),$result);
                        break;
                    case 'getSubCategorias':
                        get_sub_categorias($_GET['nome'], $db);
                        break;
                }
            break;

            case "insert":
                echo 'A inserir...';
                switch ($_GET["table"]) {
                    case "categoria_simples":
                        $db->beginTransaction();
                        $sth = $db->prepare("INSERT INTO categoria VALUES(?);");
                        $sth->execute(array($_GET["nome"]));
                        $sth = $db->prepare("INSERT INTO categoria_simples VALUES(?);");
                        $sth->execute(array($_GET["nome"]));
                        $db->commit();
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=categoria_simples" />';
                        break;
                    case "super_categoria":
                        $db->beginTransaction();
                        $sth = $db->prepare("INSERT INTO categoria VALUES(?);");
                        $sth->execute(array($_GET["nome"]));
                        $sth = $db->prepare("INSERT INTO super_categoria VALUES(?);");
                        $sth->execute(array($_GET["nome"]));
                        $db->commit();
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=super_categoria" />';
                        break;
                    case "constituida":
                        $db->beginTransaction();
                        $sth = $db->prepare("INSERT INTO constituida VALUES(?, ?);");
                        $sth->execute(array($_GET["super_categoria"], $_GET["categoria"]));
                        $db->commit();
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=constituida" />';
                        break;
                    case "fornecedor":
                        $sth = $db->prepare("INSERT INTO fornecedor VALUES(?, ?);");
                        $sth->execute(array($_GET["nif"], $_GET["nome"]));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=fornecedor" />';
                        break;
                    case "fornecedorSec":
                        $sth = $db->prepare("INSERT INTO fornece_sec VALUES(?, ?);");
                        $sth->execute(array($_GET["nif"], $_GET["ean"]));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=fornece_sec" />';
                        break;
                        
                    case "produto":
                        $db->beginTransaction();
                        $sth = $db->prepare("INSERT INTO fornecedor(nif, nome) SELECT ?,? WHERE NOT EXISTS (SELECT nif, nome FROM fornecedor WHERE nif = ?);");
                        $sth->execute(array($_GET["forn_primario_nif"],$_GET["forn_primario_nome"], $_GET["forn_primario_nif"]));
                        
                        $sth = $db->prepare("INSERT INTO produto VALUES(?,?,?,?,?);");

                        $sth->execute(array(intval($_GET['ean']), $_GET['design'], $_GET['categoria'], $_GET['forn_primario_nif'], $_GET['data']));
                        $sth = $db->prepare("INSERT INTO fornecedor(nif, nome) SELECT ?,? WHERE NOT EXISTS (SELECT nif, nome FROM fornecedor WHERE nif = ?);");
                        $sth->execute(array($_GET["forn_secundario_nif"],$_GET["forn_secundario_nome"], $_GET["forn_secundario_nif"]));
                        

                        $sth = $db->prepare("INSERT INTO fornece_sec VALUES(?, ?);");
                        $sth->execute(array($_GET["forn_secundario_nif"],$_GET["ean"]));
                        
                        $db->commit();
                        //$sth = $db->prepare("INSERT INTO fornece_sec VALUES(?, ?);");
                        //$sth->execute(array($_GET["forn_secundario_nif"],$_GET["ean"]));

                        
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=produto" />';
                        break;
                }
                break;

            case "alter":
                echo "A alterar...";
                switch ($_GET["table"]) {
                    case 'produto':
                        $sth = $db->prepare("UPDATE produto SET design =? WHERE ean = ?;");
                        $sth->execute(array($_GET["design"],intval($_GET["ean"])));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=alterDesign" />';
                        break;
                }
            break;

            case "remove":
                echo 'A remover...';
                switch ($_GET["table"]) {
                    case "categoria":
                        $sth = $db->prepare("DELETE FROM categoria WHERE nome = ?;");
                        $sth->execute(array($_GET["nome"]));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=removeCategoria" />';
                        break;
                    case "fornecedor":
                        $sth = $db->prepare("DELETE FROM fornecedor WHERE nif=?;");
                        $sth->execute(array($_GET["nif"]));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=removeFornecedor" />';
                        break;
                    case "constituida":
                        $sth = $db->prepare("DELETE FROM constituida WHERE super_categoria=? AND categoria=?;");
                        $sth->execute(array($_GET["super_categoria"], $_GET["categoria"]));
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=removeConstituida" />';
                        break;
                    case "produto":
                        $db->beginTransaction();
                        $sth = $db->prepare("DELETE FROM produto WHERE ean=?;");
                        $sth->execute(array($_GET["ean"]));
                        $sth = $db->prepare("DELETE FROM fornece_sec WHERE ean=?;");
                        $sth->execute(array($_GET["ean"]));
                        $db->commit();
                        echo '<br><meta http-equiv="refresh" content="0;bd.php?action=consult&table=removeProduto" />';
                        break;
                }
                break;
        }
    
        $db = null;
    }
    catch (PDOException $e)
    {
        echo("<p>ERROR: {$e->getMessage()}</p>");
        $db->rollBack();
    }

    function print_table($headers, $data) {
        echo '<table class="table table-bordered table-hover"><thead><tr>';
        foreach ($headers as $header) {
            echo '<th scope="col">' . $header .'</th>';
        }
        echo '</tr></thead><tbody>';
        foreach ($data as $d) {
            echo '<tr>';
            foreach ($headers as $header) {
                echo "<td>{$d[$header]}</td>";
            }
            echo '</tr>';
        }
        echo '</tbody></table>';
    } 

    function delete_table($headers, $data, $table) {
        echo '<table class="table table-bordered table-hover"><thead><tr>';
        foreach ($headers as $header) {
            echo '<th scope="col">' . $header .'</th>';
        }
        echo '</tr></thead><tbody>';
        foreach ($data as $d) {
            echo '<tr>';
            foreach ($headers as $header) {
                echo "<td>{$d[$header]}</td>";
            }
            echo '<td><a href="?action=remove&table='.$table;
            foreach ($headers as $header) {
                echo '&' . $header . '=' . $d[$header];
            }
            echo '">Remover</a></td>';
            echo '</tr>';
        }
        echo '</tbody></table>';
    }

    function alter_design($headers, $data) {
        echo '<table class="table table-bordered table-hover"><thead><tr>';
        foreach ($headers as $header) {
            echo '<th scope="col">' . $header .'</th>';
        }
        echo '</tr></thead><tbody>';
        foreach ($data as $d) {
            echo '<tr>';
            foreach ($headers as $header) {
                echo "<td>{$d[$header]}</td>";
            }
            //$d["ean"]
            echo '<td><a href="#" data-toggle="modal" data-target="#alterDesign" onclick="alterDesignModal('.$d["ean"] .')"';

            
            echo '">Alterar designação</a></td>';
            echo '</tr>';
        }
        echo '</tbody></table>';
    }

    function get_sub_categorias($nome, $db) {
        
            $sql="WITH RECURSIVE search_constituida(super_categoria, sub_categoria, depth, parents, cycle) AS (
                        SELECT c.super_categoria, c.categoria, 1,
                               ARRAY[c.super_categoria]::VARCHAR(80)[],
                               false
                        FROM constituida c
                        WHERE c.super_categoria = ?
                    UNION ALL
                        SELECT c.super_categoria, c.categoria, sc.depth + 1,
                               (parents || c.super_categoria)::VARCHAR(80)[],
                               c.categoria = ANY(parents)
                        FROM constituida c, search_constituida sc
                        WHERE c.super_categoria = sc.sub_categoria AND NOT cycle
                )
                SELECT distinct on (sub_categoria) sub_categoria, parents FROM search_constituida WHERE cycle = false;";
            $sth = $db->prepare($sql);
            $sth->execute(array($nome));
            $result = $sth->fetchAll();
            print_table(array('sub_categoria', 'parents'),$result);
    }

    ?>

  </body>

<script type="text/javascript">
    function alterDesignModal(ean) {
        $("#alterDesignEan").val(ean);
    }
</script>

<div class="modal fade" id="insereSuperCategoria" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir Super-Categoria</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="super_categoria" />
            Nome: <input type="nome" name="nome" />
            <br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>
<div class="modal fade" id="insereCategoriaSimples" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir Categoria-Simples</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="categoria_simples" />
            Nome: <input name="nome" />
            <br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>
<div class="modal fade" id="insereConstituida" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir Sub-Categoria</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="constituida" />
            Super-Categoria: <input name="super_categoria" /><br><br>
            Sub-Categoria: <input name="categoria" />
            <br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="insereFornecedor" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir Fornecedor</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="fornecedor" />
            NIF: <input name="nif" /><br><br>
            Nome: <input name="nome" />
            <br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>
<div class="modal fade" id="insereFornecedorSec" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir Fornecedor secundário</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="fornecedorSec" />
            NIF: <input name="nif" /><br><br>
            ean: <input name="ean" />
            <br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>
<div class="modal fade" id="insereProduto" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Inserir novo produto</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="insert" />
            <input type="hidden" name="table" value="produto" />
           Ean: <input name="ean" /><br><br>
           Designação: <input name="design" /><br><br>
           Categoria: <input name="categoria" /><br><br>
           Fornecedor primario Nif: <input name="forn_primario_nif" /><br><br>
           Fornecedor primario Nome *: <input name="forn_primario_nome" /><br><br>
           Fornecedor secundario Nif: <input name="forn_secundario_nif" /><br><br>
           Fornecedor secundario Nome *: <input name="forn_secundario_nome"/><br><br>
           Data: <input name="data"/><br><br><br>
           * Este campo só irá ser utilizado caso o fornecedor não exista.

            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="searchReposicao" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Consultar reposição de produto</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="specialConsult" />
            <input type="hidden" name="table" value="reposicao" />
            Ean do produto: <input name="ean" /><br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>


<div class="modal fade" id="alterDesign" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Insira a nova designação</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="alter" />
            <input type="hidden" name="table" value="produto" />
            <input  id="alterDesignEan" type="hidden" name="ean" value="" />
           Nova designação: <input name="design" /><br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>


<div class="modal fade" id="getSubCategorias" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Nome da super categoria</h4>
      </div>
      <div class="modal-body">
        <form action="bd.php" method="GET">
            <input type="hidden" name="action" value="specialConsult" />
            <input type="hidden" name="table" value="getSubCategorias" />
           Nome: <input name="nome" /><br><br>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="submit" class="btn btn-primary">Submeter</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>
</html>



<!-- coisas que podem ser uteis : 
    INSERT INTO super_categoria(nome) SELECT ? WHERE NOT EXISTS (SELECT nome FROM super_categoria WHERE nome = ?);
-->
