<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-briefcase"></span>
        <span id="form-titulo">Nova Empresa</span>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body">
        <form id="form-empresa" class="form-horizontal">

            <input type="hidden" id="id"/>

            <div class="form-group">
                <label class="col-sm-2 control-label">Nome <span class="text-danger">*</span></label>
                <div class="col-sm-6">
                    <input type="text" id="nome" class="form-control"
                           placeholder="Razão social" maxlength="255"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">CNPJ</label>
                <div class="col-sm-4">
                    <input type="text" id="cnpj" class="form-control"
                           placeholder="Ex: 12.345.678/0001-99" maxlength="18"/>
                    <span class="help-block">Com ou sem formatação.</span>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-6">
                    <button type="button" id="btn-salvar" class="btn btn-primary">
                        <span class="glyphicon glyphicon-floppy-disk"></span>
                        <span id="btn-label">Cadastrar</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/empresa/lista.jsp"
                       class="btn btn-default">
                        <span class="glyphicon glyphicon-arrow-left"></span> Voltar
                    </a>
                </div>
            </div>

        </form>
    </div>
</div>

<script>
var ctxPath = '${pageContext.request.contextPath}';

$(document).ready(function () {

    var params = new URLSearchParams(window.location.search);
    var id = params.get('id');

    if (id) {
        $('#form-titulo').text('Editar Empresa #' + id);
        $('#btn-label').text('Atualizar');

        $.ajax({
            url: ctxPath + '/api/empresa?id=' + id,
            type: 'GET',
            dataType: 'json',
            success: function (e) {
                $('#id').val(e.id);
                $('#nome').val(e.nome);
                $('#cnpj').val(e.cnpj || '');
            },
            error: function () {
                exibirAlerta('danger', 'Empresa não encontrada.');
            }
        });
    }

    $('#btn-salvar').on('click', function () {

        var payload = {
            nome: $('#nome').val().trim(),
            cnpj: $('#cnpj').val().trim() || null
        };
        var idVal = $('#id').val();
        if (idVal) payload.id = parseInt(idVal, 10);

        $.ajax({
            url: ctxPath + '/api/empresa',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            dataType: 'json',
            success: function (resp) {
                if (resp.success) {
                    exibirAlerta('success', resp.message);
                    if (!$('#id').val()) {
                        $('#nome').val('');
                        $('#cnpj').val('');
                    }
                } else {
                    exibirAlerta('danger', resp.message);
                }
            },
            error: function (xhr) {
                var msg = 'Erro ao salvar empresa.';
                try { msg = JSON.parse(xhr.responseText).message || msg; } catch (ex) {}
                exibirAlerta('danger', msg);
            }
        });
    });

    function exibirAlerta(tipo, mensagem) {
        $('#alerts').html(
            '<div class="alert alert-' + tipo + ' alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span></button>' +
            mensagem +
            '</div>'
        );
        setTimeout(function () {
            $('#alerts .alert').fadeOut(400, function () { $(this).remove(); });
        }, 3000);
    }
});
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
