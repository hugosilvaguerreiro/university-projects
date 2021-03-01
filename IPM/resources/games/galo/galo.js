// potential wins:
// [top, middle, bottom, left, center, right, topleft-bottomRight, topRight-bottomLeft]
var counters = [
  [0, 0, 0, 0, 0, 0, 0, 0],
  [0, 0, 0, 0, 0, 0, 0, 0]
]
var player = 0;
var moves = 0;
var end = false;

function updateCounters(row, column) {

  // increase the row/column counters for the players move
  counters[player][row]++;
  counters[player][column + 3]++;

  // check if move is on a diagonal
  if (row == column) {
    counters[player][6]++;
  }
  if (row + column == 2) {
    counters[player][7]++;
  }
}

function checkWinner() {

  // check if player has won (line of 3)
  var win = counters[player].indexOf(3);
  if (win != -1) {
    end = true;
    $("#result").html(["Circulos", "Cruzes"][player] + " Venceram!");
    $("#resbutmesa").css("display", "unset");
    $("#resbut2").css("display", "unset");

    if (win < 3) {
      // row win
      _(3).times(function(n) {
        cellNo = (win * 3) + n;
        $("#" + cellNo).css("color", "#22ff00");
        $("#" + cellNo).css("background-color", "#0b4f00");
      });
    } else if (win < 6) {
      // column win
      _(3).times(function(n) {
        cellNo = (n * 3) + win - 3;
        $("#" + cellNo).css("color", "#22ff00");
        $("#" + cellNo).css("background-color", "#0b4f00");
      });
    } else if (win == 6) {
      // diagonal TL-BR win
      _(3).times(function(n) {
        cellNo = 4*n;
        $("#" + cellNo).css("color", "#22ff00");
        $("#" + cellNo).css("background-color", "#0b4f00");
      });
    } else {
      // diagonal TR-BL win
      _(3).times(function(n) {
        cellNo = 2*(n+1);
        $("#" + cellNo).css("color", "#22ff00");
        $("#" + cellNo).css("background-color", "#0b4f00");
      });
    }
  }

  // check if board full
  if (moves >= 9 && win<0) {
    end = true;
    $("#result").html("Empate!");
    $("#resbutmesa").css("display", "unset");
  }
}

function computerMove() {
  // board for best potential places
  var board = [
    [0, 0, 0],
    [0, 0, 0],
    [0, 0, 0]
  ];

  // goes through each win counter
  _(3).times(function(i) {
    _(3).times(function(j) {
      
      // checks if cell full
      var cellNo = 3 * i + j;
      if ($("#" + cellNo).html() != "") {
        board[i][j] = -50;
      }

      // rows/columns
      // computers counters
      board[i][j] += 2 * Math.pow(counters[player][i], 3);
      board[j][i] += 2 * Math.pow(counters[player][i + 3], 3);

      // players counters
      board[i][j] += Math.pow(counters[(player + 1) % 2][i], 3);
      board[j][i] += Math.pow(counters[(player + 1) % 2][i + 3], 3);

      // different counters on the same row
      if (counters[0][i] > 0 && counters[1][i] > 0) {
        board[i][j] -= 3;
      }

      // different counters on the same column
      if (counters[0][i + 3] > 0 && counters[1][i + 3] > 0) {
        board[j][i] -= 3;
      }
    });

    // diagonals
    // computers counters
    board[i][i] += 2 * Math.pow(counters[player][6], 3);
    board[i][2 - i] += 2 * Math.pow(counters[player][7], 3);

    // players counters
    board[i][i] += Math.pow(counters[(player + 1) % 2][6], 3);
    board[i][2 - i] += Math.pow(counters[(player + 1) % 2][7], 3);

    // different counters on the diagonals
    if (counters[0][6] > 0 && counters[1][6] > 0) {
      board[i][i] -= 3;
    }
    if (counters[0][7] > 0 && counters[1][7] > 0) {
      board[i][2 - i] -= 3;
    }
  });
  
  var max = 0;
  var maxList = [];
  // get an array of all occurences of the 'best' position
  _.each(_.flatten(board), function(n, ind) {
    // new maximum or add to old maximum
    if (n > max) {
      max = n;
      maxList = [ind];
    } else if (n == max) {
      maxList.push(ind);
    }
  });

  // randomly pick from list
  var cell = _.shuffle(maxList)[0];

  // display
  $("#" + cell).html("OX" [player]);

  return [Math.floor(cell / 3), cell % 3];
}

function reset() {

  // remove O/X from each cell, reset color
  $(".cell").each(function() {
    $(this).html("");
    $(this).css("color", "white");
    $(this).css("background-color", "black");
  });

  //reset rematch visibility
  $("#resbutmesa").css("display", "none");
  $("#resbut2").css("display", "none");

  // reset result
  $("#result").html("");

  // reset variables
  counters = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0]
  ];
  player = 0;
  moves = 0;
  end = false;
}

$(function() {

  // when a cell is clicked
  $(".cell").on(
    "click",
    function(eventObject) {

      // no winner/draw yet
      if (!end) {
        // get cell div
        var cell = eventObject.target;

        // row and column of cell
        var cellRow = Math.floor(cell.id / 3);
        var cellCol = cell.id % 3;

        // check position is empty
        if (cell.innerHTML == "") {

          // display O/X
          cell.innerHTML = "OX" [player];

          // increase moves taken
          moves++;

          // update the counters for the potential wins
          updateCounters(cellRow, cellCol);

          // check for a winner/draw
          checkWinner();

          // next player
          player = (player + 1) % 2;

          if ($(".players:checked").val() == 1 && !end) {
            // computers turn
            var compCell = computerMove();

            // update the counters for potential wins
            updateCounters(compCell[0], compCell[1]);

            // increase moves taken
            moves++;

            // check for a winner/draw
            checkWinner();

            // next player
            player = (player + 1) % 2;
          }
        }
      }
    });

  // reset button
  $(".reset").on(
    "click",
    reset);

  // change number of players
  $(".players").on(
    "change",
    reset);
});