/*##############################################################################*/
/*
     creates and returns a new simulation world that fills the entire given div

 divID- id of the div that will contain the world
 --optional--
 options- object of the type {loopCount: number} representing the number of loops to do each time simulation is called
*/
var interval2
function createWorld(divId, options) {
    var div = document.getElementById(divId);
    var height = $(div).height();
    var width = $(div).width();

    var canvas = Raphael(divId, width, height);
    if (options)
        return new World(canvas, options);
    else
        return new World(canvas);

}

function openSideMenu(type, interface, subFolder, width) {
    var width = width || 255
    if (interface.popUp) {
        interface.popUp.animate("shake");
    } else {
        if (interface.sideMenu && interface.sideMenu.type == type) {
            interface.removeSideMenu();
        } else {
            interface.createSideMenu(width, 565, {
                x: w - buttonSize - 40 - width,
                y: h - 4 * buttonSize - 12 * 20
            }, type);
            if (subFolder)
                html = "resources/htmls/" + subFolder + "/" + type + ".html";
            else
                html = "resources/htmls/" + type + ".html";
            interface.sideMenu.appendNewDiv("interface", type, html);
            w3IncludeHTML();
            $("#" + type).hide();
            $("#" + type).show("slide", {
                direction: "right"
            });
        }
    }
}


function openBigMenu(type, interface, subFolder) {
    if (interface.popUp) {
        interface.popUp.animate("shake");
    } else {
        if (interface.bigMenu && interface.bigMenu.type != type)
            interface.removeBigMenu();

        interface.createBigMenu(500, 500, {
            x: window.innerWidth/20,
            y: window.innerHeight/2-250
        }, type);
        if (subFolder)
            html = "resources/htmls/" + subFolder + "/" + type + ".html";
        else
            html = "resources/htmls/" + type + ".html";
        interface.bigMenu.appendNewDiv("interface", type, html);
        w3IncludeHTML();
        setTimeout(function() {document.getElementById("hammerTemp").click()}, 200)

    }

}

function createButtons() {
    //Buttons
    var i = 0;
    var buttons = [];
    if (!$("#waiter").length) {
        buttons[i++] = interface.createButton({
            x: w - buttonSize - 300,
            y: 20
        }, 135, 385, "waiter", "resources/htmls/buttons/waiter.html");

    }

    buttons[i++] = interface.createButton({
        x: w - buttonSize - 40,
        y: h - 4 * buttonSize - 12 * 20
    }, 565, 120, "sideButtons", "resources/htmls/buttons/sideButtons.html");

    w3IncludeHTML();
    return buttons;
}
var indexOrder = 0;

function createOrderButtons() {
    //Buttons
    var i = 0;
    var buttons = [];

    buttons[i++] = interface.createButton({
        x: w - buttonSize - 40,
        y: h - 4 * buttonSize - 12 * 20
    }, 565, 120, "orderButtons", "resources/htmls/buttons/orderButtons.html");
    w3IncludeHTML();
    return buttons;
}

function changeWaiter(mode) {
    if (mode) {
        $("#waiter").find("#sideButton").prop('onclick', null).off('click').on("click", function() {
            interface.popup(300, 200, {
                x: w - buttonSize - 440 - 40,
                y: 20
            }, 'resources/htmls/popUps/empregado2.html');
        }).addClass('btn-warning').removeClass('btn-info');
    } else {
        $("#waiter").find("#sideButton").prop('onclick', null).off('click').on("click", function() {
            interface.popup(300, 200, {
                x: w - buttonSize - 440 - 40,
                y: 20
            }, 'resources/htmls/popUps/empregado.html');
        }).addClass('btn-info').removeClass('btn-warning');
    }
}

function getScrollbarWidth() {
    var outer = document.createElement("div");
    outer.style.visibility = "hidden";
    outer.style.width = "100px";
    outer.style.msOverflowStyle = "scrollbar";

    document.body.appendChild(outer);

    var widthNoScroll = outer.offsetWidth;
    outer.style.overflow = "scroll";

    var inner = document.createElement("div");
    inner.style.width = "100%";
    outer.appendChild(inner);

    var widthWithScroll = inner.offsetWidth;

    outer.parentNode.removeChild(outer);

    return  widthNoScroll - widthWithScroll;
}

function empregado(control) {
    var control = control || 0
    var empregado = $("#waiter").find("#sideButton");
    var loadingBar ='<div class="progress progress-striped" style="margin-top:5px; z-index:1;"><div class="progress-bar progress-bar-info" id="loading" style="width: 1%"></div></div>'
    empregado.append(loadingBar)
    var $item = empregado.find(".progress");
    console.log($item)
    if(control)
        load($item, 1)
    else
        load($item)
}

function load($item, control, interval) {
  var control = control || 0
  var elem = $item;
  interval = interval || 100;
  var width = 1;
  interval2 = setInterval(frame, interval);
    var empregado = $("#waiter").find("#sideButton");
  function frame() {
    if (width == 110) {
        clearInterval(interval2);
        empregado.find(".progress").remove()
        changeWaiter(0)
        if(control) {
            cleanPayment()
            interface.popup(300 , 150, {"x":w/2, "y":h/2}, "resources/htmls/popUps/confirmarPagamento.html");
        }
    }
    else if (width < 100){
      width++;
      elem.find("#loading").css({"width" : width + '%'});
    }
    else
        width++
  }
}

function removeLoad() {
     var empregado = $("#waiter").find("#sideButton");
    clearInterval(interval2)
    empregado.find(".progress").remove()
    console.log("cancel")
}
