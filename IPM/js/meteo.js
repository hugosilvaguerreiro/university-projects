var cities = {Lisboa:0, Beja:0, Bragança:0, Faro:0, Manteigas:0, Sagres:0, Sintra:0, Viseu:0};
loadedCity = "";

function loadCity(name, displayName) {
  var wasLoaded = cities[displayName];
  if(loadedCity != displayName) {
    loadedCity = displayName;
    document.getElementById("cityName").innerHTML = '<i class="fa fa-spinner fa-spin" style="margin-right: 10px;"></i>';
    $("#dropdown1").animate({opacity: 0}, 200, function() {
        loadCityAux(name, displayName);
        if(wasLoaded == 0) {
          setTimeout(function() {document.getElementById("cityName").innerHTML = displayName;}, 1500)
          $("#dropdown1").delay(1500).animate({opacity: 1}, 200);
        } else {
          document.getElementById("cityName").innerHTML = displayName;
          $("#dropdown1").animate({opacity: 1}, 200);
        }
    });
  }
}

function loadCityAux(name, displayName) {
    if(cities[displayName] == 0) {
      firstLoad(name, displayName);
    } else {
      loadStuff(name, displayName);
    }
}

function firstLoad(name, displayName) {
    $.ajax({
          url : "http://api.wunderground.com/api/a1e0af8cc14bbe83/geolookup/conditions/forecast10day/lang:BR/q/Portugal/"+name+".json",
          dataType : "jsonp",
          success : function(parsed_json) {
              cities[displayName] = parsed_json;
              loadStuff(name, displayName);
          }
    });
}

function loadStuff(name, displayName) {
  for (i = 0; i < 6; i++) {
      document.getElementById("date" + i).innerHTML = cities[displayName]['forecast']['simpleforecast']['forecastday'][i]['date']['weekday_short'];
      document.getElementById("icon" + i).src = "http://icons.wxug.com/i/c/k/" +
      cities[displayName]['forecast']['simpleforecast']['forecastday'][i]['icon'] + ".gif";
      temp = cities[displayName]['forecast']['simpleforecast']['forecastday'][i]['high']['celsius'];
      document.getElementById("tempHigh" + i).innerHTML = (temp === "")? "Indisponível" : temp + " ºC";
      temp = cities[displayName]['forecast']['simpleforecast']['forecastday'][i]['low']['celsius'];
      document.getElementById("tempLow" + i).innerHTML = (temp === "")? "Indisponível" : temp + " ºC";
  }
}
