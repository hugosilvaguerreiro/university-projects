var animation = false;
var id3 = '';
var acompa = '';
var acompanhamento = ['Batatas fritas', 'Batatas cozidas', 'Arroz', 'Feijão', 'Ovo', 'Salada', 'Croutons', 'Molho'];
var oldvalue = 0;
var clicked = false;
var inOrder = 0;
var hammerxD = 0;
function changePrice(price) {
  var total = $("#orderShower").find("#total").html();
  total = parseFloat(total[6]+ total[7]+total[8]+total[9]) + price;
  $("#orderShower").find("#total").html("Total: "+total+"&euro;");
  var total2 = $("#hiddenTrash").data("totalTemp");
    total2 += price;
    $("#hiddenTrash").data("totalTemp", total2);


}

function recycleImage(id, price, hammer) {
    var hammer = hammer || false;
    var $gallery = $("#gallery"),
        $trash = $("#trash2");
    var $item = $(id);
    var n = parseInt($item.find(".badge").html());
    if (n == 1 || hammer) {
        $item.fadeOut(function() {
            $item.remove();
        });
        inOrder--;
        if($("#trash2").length == 0){
          $("#trash2").toggle("puff");
        }
    } else {
        $item.find(".badge").html(n - 1);
    }
    if(!hammer)
        changePrice(-1*parseFloat(price));

    if(inOrder == 0) {
        $("#trashTop").css({"visibility": "hidden"});
        $trash.html("");
        $trash.css({"overflow-y": ""});
        $("#orderShower").find("#confirmOrder").addClass("disabled");
    }
}

function addToTrash($item, id, content, quantity, price, name, trash) {

    var name = name || "";
    var quantity = quantity || 1;
    var content = content || '00000000';
    var item = $item.clone();
    inOrder++;

    if( id != $item.attr("id")) {
        var name = $item.find("h5").html();
        item.find("h5").html(name+' *');
    }
    var id2 = id + "2" + content;
    item.find(".badge").remove();
    item.find("i").append("<span class='badge' style='font-size:15px; margin-left:10px; margin-bottom:2px '>" + quantity + "</span>")
    item.attr("id", id2);
    item.find("i.fa-pencil-square-o ").css({
        "margin-right": "12px"
    }).prop('onclick',null).off('click').click(function(){
        if(!interface.popUp)
            showDetails2(id2, content, price, quantity);
    })
    item.data("price", price)
    var html = 'resources/htmls/order/templates/FoodTemplate2.html';
        var pos = {
            x: 190,
            y: 0
        };


    var recycle_icon = "<i class='fa fa-trash ' aria-hidden='true'"  + " title='anular'></i>";
    var $trash = trash || $("#trash2");
    $trash.css({
        "overflow-y": "scroll"
    })

    var $list = $("ul", $trash).length ?
        $("ul", $trash) :
        $("<ul class='gallery ui-helper-reset'>").appendTo($trash);

    item.append(recycle_icon).appendTo($list).fadeIn(function() {
        item.find("i.fa-plus-circle").remove();
        item.animate({
                width: "115px"
            })
            .find("img")
            .animate({
                height: "72px"
            });
    });

    item.find(".fa-trash").click(function(){
        var position = 140;

        interface.popup(300 , 150, {"x":position, "y":h -h/4 }, "resources/htmls/order/PopUps/cancelarPedido2.html");
        setTimeout(function() {
            $("#popUP").find(".btn-success").off("click").click(function(){
                var n = parseInt($("#"+id2).find(".badge").html());
                changePrice(-1*n*price);
                recycleImage("#"+id2,price,true)
            interface.removePopUp();});

        },200)
    });
    item.data("price", price);
    item.data("quantity", quantity);
    $("#orderShower").find("#confirmOrder").removeClass("disabled");
    $("#trashTop").css({"visibility": "visible"});
    startTrash();
}


function checkContent($item, id, div, quantity, price) {

    $trash = $("ul", $("#trash2"));
    var quantity = quantity || 1;
    var div = div;
    if ($trash.length != 0) {
        var len = 0;
        $trash.find("li").each(function() {
            var name = $(this).attr("id").split("2");
            if (name[1] == div && id == name[0]) {
                var n = $(this).find(".badge").html();
                $(this).find(".badge").html(parseInt(n) + quantity);
                changePrice(quantity*price);
                len++;
                if (!animation) {
                    if (!$("#trash2").is(":visible"))
                        $('#trash2').toggle('puff')
                    animation = true;
                    var transferHelper = $item.clone().css({
                        opacity: 0.3
                    });
                    $item.effect('transfer', {
                        clone: transferHelper,
                        to: $(this)
                    }, 500, function() {
                        animation = false
                    }, 250);
                    $(this).effect("highlight");
                }
                    var quant = $(this).data("quantity") + quantity;
                    $(this).data("quantity",quant);
                    $(this).find("i.fa-pencil-square-o ").prop('onclick',null).off('click').click(function(){
                    showDetails2(name[0]+"2"+name[1], div, price,parseInt(n)+ parseInt(quantity),name);})
                    startTrash();
            }
        });
        if (len == 0) {
            if (!$("#trash2").is(":visible"))
                $('#trash2').toggle('puff')
              changePrice(quantity*price);
            addToTrash($item, id, div, quantity, price);
        }
    } else {
        if (!$("#trash2").is(":visible"))
            $('#trash2').toggle('puff')
          changePrice(quantity*price)
        addToTrash($item, id, div, quantity, price);

    }

}

function deleteImage($item, id, div, price) {
        var quantity = parseInt($("#popUP").children().find("#counter").html());
        checkContent($item, id, div, quantity, price);

}

function deleteImageFix($item, id, div, price) {
    if(interface.popUp)
        interface.popUp.animate("shake");
    else
        checkContent($item, id, div, 1, price);
}



String.prototype.replaceAt=function(index, replacement) {
    return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
}


function showDetails(id, parent, name, price, quantity, acompanhamentos, pos) {
    var acompa = acompanhamentos || '00000000';
    if (interface.popUp) {
        interface.popUp.animate("shake");
    } else {
        var pos = pos || {
            x: w - buttonSize - 40 -288 - getScrollbarWidth(),
            y: h - 4 * buttonSize - 12 * 20
        };

        var html = 'resources/htmls/order/templates/FoodTemplate.html';
        interface.popup(288 + getScrollbarWidth(), 800, pos, html);
        setTimeout(function() {
            var imagemDiv = $("#popUP").children().find("#tempBody");
            for (i = 0; i < acompa.length; i++) {
                if (acompa[i] === "1") {
                    var label = '<input id="' + 'box' + i + '" type="checkbox"  checked/>';
                    label += '<label for="box' + i + '" class="label label-success" style="font-size: 15px;margin-left: 25%;" >' + acompanhamento[i] + '</label><br><br>';
                    imagemDiv.find("#acompanhamentos").append(label);
                }
            }
            if (acompa.includes("1")) {
                imagemDiv.find("#acompanhamentos").prepend('<p style="text-align: center">Acompanhamentos</p>');
                imagemDiv.find("#acompanhamentos").append('<hr>');
            }
            var imagem = '<img src="resources/Images/food/' + parent + "/" + id + '.png" width="120" height="100" style="position: relative; left: 25%;margin: 10px 0 6px 0;" >'
            imagemDiv.prepend(imagem);
            imagemDiv.find("#caracteristicas").children().get(0).innerHTML = "Preço: " + price;
            $("#popUP").children().find("#name").html(name);
            var botao1 = "<a id='botao1' href='#' >Adicionar</a>";
            $("#botoes").append(botao1);
            $("#botao1").addClass('btn').addClass("btn-success");

            $("#botao1").click(function() {
                id3 = id;
                j = 0;
                var acomp = $(popUP).find("#acompanhamentos");
                if(acomp.find("input").length) {

                    acomp.find("input").each(function(){
                    while(acompa[j] != "1" && j<acompa.length) {
                           j++
                    }
                        if(!$(this).is(':checked') ){
                            if(acompa[j] == "1") {
                                    acompa = acompa.replaceAt(j, "0");

                                    if(id3 == id)
                                        id3 = "z"+ id3;
                            }
                        }
                        j++


                    })
                }

                deleteImage($('#' + id), id3, acompa, parseFloat(price[0]+price[1]+price[2]+price[3]), name);
                    interface.removePopUp();
            });


            var botao2 = "<a id='botao2' href='#' >Cancelar</a>";
            $("#botoes").append(botao2);
            $("#botao2").click(function() {
                interface.removePopUp()
            });
            $("#botao2").addClass('btn').addClass("btn-danger");
        }, 250);
    }

}



function showDetails2(id, acompanhamentos, price, quantity, name){
     var pos = {
            x: 190,
            y: h -310
        };

        var html = 'resources/htmls/order/templates/FoodTemplate2.html';
        interface.popup(270, 300, pos, html);
        setTimeout(function(){
        imagemDiv = $("#popUP");
        var name = $("#"+id).find("h5").html();
        imagemDiv.find("#name").html(name);
           // imagemDiv = imagemDiv.find("#acompanhamentos")
         for (i = 0; i < acompanhamentos.length; i++) {
                if (acompanhamentos[i] === "1") {
                    //var label = '<input id="' + 'box' + i + '" type="checkbox"  checked/>';
                    var label = '<label for="box' + i + '" class="label label-warning" style="font-size: 15px;margin-left: 25%;" >' + acompanhamento[i] + '</label><br><br>';
                    imagemDiv.find("#acompanhamentos").append(label);

                }
            }
            if (acompanhamentos.includes("1")) {
                imagemDiv.find("#acompanhamentos").prepend('<p style="text-align: center">Acompanhamentos</p>');
                imagemDiv.find("#acompanhamentos").append('<hr>');
            }
            else {
                imagemDiv.find("#acompanhamentos").prepend('<p style="text-align: center">Sem acompanhamentos</p>');
                imagemDiv.find("#acompanhamentos").append('<hr>');
            }
            imagemDiv.find("#counter").html($("#"+id).find(".badge").html());
              var botao1 = "<a id='botao1' href='#' >Ok</a>";
        $("#botoes").append(botao1);
        $("#botao1").addClass('btn').addClass("btn-success");
        oldvalue = parseInt($("#counter").html());
        $("#minus").off("click").click(function(){minus2(oldvalue)})
        $("#plus").off("click").click(function(){plus2(oldvalue)})
        $("#botao1").click(function(){
            var n = parseInt($("#popUP").find("#counter").html());
            var nv = n - oldvalue;
            if(nv == -1*oldvalue) {
                recycleImage("#"+id, price, true);
                changePrice(nv*price);
                oldvalue = 0;
            }
            else {
                var v = parseInt($("#"+id).find(".badge").html());
                $("#"+id).find(".badge").html(v+nv)
                changePrice(nv*price);
                oldvalue += nv;
            }
            interface.removePopUp();
        })



    }, 100);

}

function showDetails3(id, acompanhamentos, price, quantity, name){
     var pos = pos || {
            x: w - buttonSize - 40 - 288 -getScrollbarWidth(),
            y: h - 4 * buttonSize - 12 * 20
        };
        var html = 'resources/htmls/order/templates/FoodTemplate3.html';
        interface.popup(288 + getScrollbarWidth(), 800, pos, html);
        setTimeout(function(){
         imagemDiv = $("#popUP");
        imagemDiv.find("#price").html("Preço:"+price+"&euro;")
        var name = $("#"+id).find("h5").html();
        imagemDiv.find("#name").html(name);
         for (i = 0; i < acompanhamentos.length; i++) {
                if (acompanhamentos[i] === "1") {
                    var label = '<label for="box' + i + '" class="label label-warning" style="font-size: 15px;margin-left: 25%;" >' + acompanhamento[i] + '</label><br><br>';
                    imagemDiv.find("#acompanhamentos").append(label);

                }
            }
            if (acompanhamentos.includes("1")) {
                imagemDiv.find("#acompanhamentos").prepend('<p style="text-align: center">Acompanhamentos</p>');
                imagemDiv.find("#acompanhamentos").append('<hr>');
            }
            else {
                imagemDiv.find("#acompanhamentos").prepend('<p style="text-align: center">Sem acompanhamentos</p>');
                imagemDiv.find("#acompanhamentos").append('<hr>');
            }

    }, 200);

}

function startHiddenTrash() {
     setTimeout(function() {
        var $gallery = $("#meuPedido");
            $("#gallery").find("li").each(function(){
                var id = $(this).attr("id");
               $("#"+id).find("img").off("click").on("click", function(){
                    $("#"+id).find(".fa-pencil-square-o ").click();
               })
            })
    }, 250);
}

function startHiddenTrash2() {
     setTimeout(function() {
        var $gallery = $("#meuPedido");
        $("li", $gallery).each(function() {
            $(this).find("img").off("click").on('click', function() {
                $(this).next(".fa-pencil-square-o ").click();
            });

        });
    }, 200);
}

function startTrash() {
     setTimeout(function() {
        var $gallery = $("#trash2");
        $("li", $gallery).each(function() {
            $(this).find("img").off("click").on('click', function() {
                $(this).next(".fa-pencil-square-o ").click();
            });

        });
    }, 200);
}

function startGallery() {
    setTimeout(function() {
        var $gallery = $("#gallery");
        $("li", $gallery).each(function() {
            $(this).find("img").on('click', function() {
                $(this).next(".fa-pencil-square-o ").click();
            });

        });
    }, 200);

}

function minus() {
    var n = document.getElementById("counter").innerHTML;
    var b = parseInt(n);
    if (b - 1 >= 0) {
        b -= 1;
        document.getElementById("counter").innerHTML = b;
        $("#caracteristicas").addClass("changedQuantity").removeClass("default");
    }
}

function plus() {
    var n = document.getElementById("counter").innerHTML;
    var b = parseInt(n);
    if (b + 1 <= 99) {
        b += 1;
        document.getElementById("counter").innerHTML = b;
        $("#caracteristicas").addClass("changedQuantity").removeClass("default");
    }
    if (b == 1) {
        $("#caracteristicas").addClass("default").removeClass("changedQuantity");
    }
}

function minus2(oldvalue) {
    var botao = $("#botao1");
    var n = document.getElementById("counter").innerHTML;
    var b = parseInt(n);
    if (b - 1 >= 0) {
        b -= 1;
        document.getElementById("counter").innerHTML = b;
        $("#caracteristicas").addClass("changedQuantity").removeClass("default");
    }
    if(b!= oldvalue) {
        botao.html("Atualizar")
    }
    if(b==oldvalue){
        botao.html("Ok")
    }
}

function plus2(oldvalue) {
    var botao = $("#botao1");
    var n = document.getElementById("counter").innerHTML;
    var b = parseInt(n);
    if (b + 1 <= 99) {
        b += 1;
        document.getElementById("counter").innerHTML = b;
        $("#caracteristicas").addClass("changedQuantity").removeClass("default");
    }
    if (b == 1) {
        $("#caracteristicas").addClass("default").removeClass("changedQuantity");
    }
    if(b!= oldvalue) {
        botao.html("Atualizar")
    }
    if(b== oldvalue) {
        botao.html("Ok")
    }
}

function confirm() {
    var total = $("#orderShower").find("#total").html();

    if(inOrder != 0) {
        interface.popup(300 , 300, {"x":110, "y":h -h/4}, "resources/htmls/order/PopUps/finalizarPedido.html");
        setTimeout(function(){
            $("#popUP").find("h1").html(total)
        },100);
    }

}

function confirm2() {
   var loadingBar ='<p id="status" style="text-align:center; margin:0">A preparar</p><div class="progress progress-striped" style="margin:0; z-index:1;"><div class="progress-bar progress-bar-info" id="loading" style="width: 1%"></div></div>'
   var total = $("#hiddenTrash").data("totalTemp");
   var totalToPay = $("#paymentTrash").data("total")
    var totalTot = $("#hiddenTrash").data("total");
    $("#paymentTrash").data("total", total + totalToPay)
    $("#hiddenTrash").data("total", total+totalTot);
    $("#hiddenTrash").data("totalTemp",0);
   $("#trash2").find("li").each(function(){
        var price = $(this).data("price");
        var quantity = $(this).data("quantity");
        var $newLi = $(this).clone(true);
        var id2 = $newLi.attr("id") + "2" + hammerxD++;
        $newLi.attr("id", id2)
        var content = id2.split("2")[1];
       deleteImage2($(this), id2,content, price, quantity);
        $newLi.find(".fa-pencil-square-o ").prop('onclick',null).off('click').click(function(){
        if(!interface.popUp)
            showDetails3(id2, content, price);
        })
        $newLi.find(".fa-trash").remove();
        $newLi.find(".fa-pencil-square-o ").before(loadingBar);
        $("#hiddenTrash").find("ul").append($newLi);
        setTimeout(function(){
            moveBar($newLi, 100);
        },250);

   })
}


function moveBar($item, interval) {
    var elem = $item;
  var interval = interval || 10;
  var width = 1;
  var interval = setInterval(frame, interval);
  function frame() {
    if (width > 100 && width < 200) {
      width++;
      elem.find("#loading").css({"width" : width -100 +'%'});
    }
    else if (width == 100) {
        elem.find("#status").html("A caminho");
        elem.find("#loading").css({"width":"0%"}).removeClass("progress-bar-info").addClass("progress-bar-warning");
        width++;
    }
    else if(width >= 150) {
        clearInterval(interval);
         elem.find("#status").html("Na mesa");
        elem.find("#loading").removeClass("progress-bar-warning").addClass("progress-bar-success");
    }
    else {
      width++;
      elem.find("#loading").css({"width" : width + '%'});
    }
  }
}

function openPedido() {
    var ul = $("#hiddenTrash").find("ul");
    openSideMenu('meuPedido', interface, 'information',288+getScrollbarWidth());
    setTimeout(function(){
        var total = $("#hiddenTrash").data("total");
        if(total > 0) {
             startHiddenTrash();
            $("#meuPedido").find(".panel-body").append(ul);
            var total = $("#hiddenTrash").data("total");
            $("#meuPedido").find("#price").html("Total:"+ total+"&euro;");

        }
        else {
           var vazio = "<p>O seu pedido encontra-se vazio.</p></br>";
           var pedido = '<a href="#" class="btn btn-success" onclick="interface.startOrderMenu()" style="width:100%" >Fazer Pedido</a>';
           $("#meuPedido").find("#price").html(vazio);
           $("#meuPedido").find(".panel-body").append(pedido);
        }
    },250);
}

function removeHiddenTrash() {
    var ul = $("#meuPedido").find("ul");
    $("#hiddenTrash").append(ul);
}


function removePaymentTrash() {
    clearTimeout(time)
    var ul = $("#pagamento").find("ul");
    $("#paymentTrash").append(ul);
}


function openPayment() {
    var ul = $("#paymentTrash").find("ul");
    if(ul.children().length != 0) {
            openSideMenu('pagamento', interface, 'pagamento',288+getScrollbarWidth());
            setTimeout(function(){
            $("#pagamento").find(".panel-body").append(ul);
            var total = $("#paymentTrash").data("total")
            $("#pagamento").find("#price").html("Total:"+total+"&euro;")

    },150);
    }
    else {
        openSideMenu('pagamento2', interface, 'pagamento',288+getScrollbarWidth());
    }

}


function checkContent2($item, id, div, quantity, price) {
    $trash = $("ul", $("#paymentTrash"));

    var quantity = quantity || 1;
    var div = div;
    var id = id.split("2")[0]
    if ($trash.length != 0) {
        var len = 0;
        $trash.find("li").each(function() {
            var name = $(this).attr("id").split("2");
            if (name[1] == div && id == name[0]) {
                var n = $(this).find("#quantidade").html();
                $(this).find("#quantidade").html(parseInt(n) + quantity);
                len++;
                    var quant = $(this).data("quantity") + quantity;
                    $(this).data("quantity",quant);
                $(this).find("#preco").html((parseInt(n) + quantity)*price +"&euro;")
            }
        });
        if (len == 0) {
            addToTrash2($item, id, div, quantity, price);
        }
    } else {
        addToTrash2($item, id, div, quantity, price);

    }
}

function addToTrash2($item, id, content, quantity, price) {
    var item = $item.clone();
    var content = content
    var $trash = $("#paymentTrash");
    $trash.css({
        "overflow-y": "scroll"
    })
    var badge = item.find(".fa-pencil-square-o ").find(".badge");
    item.find(".fa-pencil-square-o ").before(badge);
    item.find(".fa-pencil-square-o ").remove()
    item.find(".fa-trash").remove();
    item.find("img").remove();
    var badge2 = item.find(".badge")
    badge2.attr("id", 'quantidade')
    var cont = ""
    for(i=0; i < content.length; i++) {
        if(content[i] == "1") {
            cont +=  "<span class='badge'>"+ acompanhamento[i] +"</span>"
        }
    }
    if(cont.length != 0)
        badge2.before("<p style='margin-top:0'>Acompanhamentos</p><div style='margin:0; ' id='contents'>" + cont +"</div><hr style='margin: 0'><p>Quantidade</p>")
    else {
         badge2.before("<p style='margin:0'>Sem acompanhamentos</p><div style='margin:0; ' id='contents'></div><hr style='margin-bottom: 0'><p>Quantidade</p>")
    }
    item.append("<hr style='margin:0'><p style='margin:0'>Sub-total</p><span id='preco' class='badge'>"+price*quantity+"&euro;</span")
     var width = 98
     item.css({"width":width+"%"})
     item.find("#quantidade").css({"margin-left":0})
    $("ul", $trash).append(item)
}




function deleteImage2($item, id, div, price, quantity) {
        checkContent2($item, id, div, quantity, price);
}

function cleanPayment() {
    $("#paymentTrash").data("total", 0)
    $("#paymentGallery").find("li").each(function(){$(this).remove()})
    interface.removeSideMenu();
    //openPayment()
}
var time
function openPayment2() {
    openSideMenu('pagamento3', interface, 'pagamento',288+getScrollbarWidth());
    setTimeout(function(){
    var total = $("#paymentTrash").data("total")
    $("#pagamento3").find("#price").html("Total:"+total+"&euro;")
},150);
    time = setTimeout(function() {
        cleanPayment();
        interface.popup(300 , 150, {"x":w/2, "y":h/2}, "resources/htmls/popUps/confirmarPagamento.html")
    }, 5000)
}
var valuein=0;
function validate(value) {
    valuein = value

}
function displayHelp() {
    $()
}
function validate2() {
    if(valuein.toString().length < 9 && valuein.toString().length > 0) {
         $(".btn-success").prop("onclick",null).off("click")
         $(".btn-success").tooltip({
            disabled: true,
            close: function( event, ui ) { $(this).tooltip('disable'); }
         })

        $(".btn-success").on('click', function () {
            $(this).tooltip('enable').tooltip('open');
            });
     }
    else {
        $(".btn-success").on("click",function(){interface.removePopUp();changeWaiter(1); empregado(1)})
    }

}
function openNif() {
    interface.popup(300 , 200, {"x":w -buttonSize - 480, "y":h-300}, "resources/htmls/popUps/nif.html")
    setTimeout(function(){$('#text-basic').numpad();
        $('#password').numpad({
                    displayTpl: '<input class="form-control" type="text" />',
                    position :   'fixed',
                    positionX:   w-buttonSize-480,
                    positionY: h-300,
                    textDone:    'Ok',
                    textDelete:  'Eliminar',
                    textClear:   'Limpar',
                    textCancel:  'Cancelar',
                    buttonNumberTpl:    '<button class="btn btn-info" style="margin:5px"></button>',
                    buttonFunctionTpl:   '<button class="btn btn-default" style="width:100%;"></button>',
                    hidePlusMinusButton: true,
                    hideDecimalButton: true,
                    gridtpl: "<table class='well'></table>",
                    onChange: function(event, value){validate(value);},
                    onKeypadClose: function(){validate2()}
                });
    },150)
            setTimeout(function(){$(".nmpd-grid").addClass("well")}, 150);
}
function openNif2() {
    interface.popup(300 , 200, {"x":w -buttonSize - 480, "y":h-300}, "resources/htmls/popUps/nif2.html")
    setTimeout(function(){$('#text-basic').numpad();
        $('#password').numpad({
                    displayTpl: '<input class="form-control" type="text" />',
                    position :   'fixed',
                    positionX:   w-buttonSize-480,
                    positionY: h-300,
                    textDone:    'Ok',
                    textDelete:  'Eliminar',
                    textClear:   'Limpar',
                    textCancel:  'Cancelar',
                    buttonNumberTpl:    '<button class="btn btn-info" style="margin:5px"></button>',
                    buttonFunctionTpl:   '<button class="btn btn-default" style="width:100%;"></button>',
                    hidePlusMinusButton: true,
                    hideDecimalButton: true,
                    gridtpl: "<table class='well'></table>"
                });
    },150)
            setTimeout(function(){$(".nmpd-grid").addClass("well")}, 150);
}
