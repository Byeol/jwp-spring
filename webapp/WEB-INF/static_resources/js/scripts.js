$(".answerWrite input[type=submit]").click(addAnswer);

function addAnswer(e) {
  e.preventDefault();

  var queryString = $("form[name=answer]").serialize();

  $.ajax({
    type : 'post',
    url : '/api/answers',
    data : queryString,
    dataType : 'json',
    error: onError,
    success : onSuccess,
  });
}

function onSuccess(json, status){
  var answerTemplate = $("#answerTemplate").html();
  var template = answerTemplate.format(json.writer, new Date(json.createdDate), json.contents, json.answerId, json.answerId);
  $(".qna-comment-slipp-articles").prepend(template);
}

function onError(xhr, status) {
  alert("error");
}

$(".qna-comment").on("click", ".form-delete", deleteAnswer);

function deleteAnswer(e) {
  e.preventDefault();

  var deleteBtn = $(this);
  var url = deleteBtn.closest("form").attr("action");

  $.ajax({
    type: 'delete',
    url: url,
    dataType: 'json',
    error: function (xhr, status) {
      if (xhr.responseJSON && xhr.responseJSON.message) {
        alert(xhr.responseJSON.message);
      } else {
        alert("error");
      }
    },
    success: function (json, status) {
      deleteBtn.closest('article').remove();
    }
  });
}

String.prototype.format = function() {
  var args = arguments;
  return this.replace(/{(\d+)}/g, function(match, number) {
    return typeof args[number] != 'undefined'
        ? args[number]
        : match
        ;
  });
};