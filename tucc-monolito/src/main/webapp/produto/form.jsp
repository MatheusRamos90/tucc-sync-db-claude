<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-tag"></span>
        <span id="form-titulo">Novo Produto</span>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body">
        <form id="form-produto" class="form-horizontal">

            <input type="hidden" id="id"/>

            <div class="form-group">
                <label class="col-sm-2 control-label">Nome <span class="text-danger">*</span></label>
                <div class="col-sm-6">
                    <input type="text" id="nome" class="form-control"
                           placeholder="Nome do produto" maxlength="255"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Valor (R$) <span class="text-danger">*</span></label>
                <div class="col-sm-4">
                    <input type="text" id="valor" class="form-control" placeholder="Ex: 99.90"/>
                    <span class="help-block">Use ponto como separador decimal (ex: 99.90)</span>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Desconto (R$)</label>
                <div class="col-sm-4">
                    <input type="text" id="desconto" class="form-control" placeholder="Ex: 10.00 (opcional)"/>
                    <span class="help-block">Deixe em branco se não houver desconto.</span>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-6">
                    <button type="button" id="btn-salvar" class="btn btn-primary">
                        <span class="glyphicon glyphicon-floppy-disk"></span>
                        <span id="btn-label">Cadastrar</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/produto/lista.jsp"
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
        $('#form-titulo').text('Editar Produto #' + id);
        $('#btn-label').text('Atualizar');

        $.ajax({
            url: ctxPath + '/api/produto?id=' + id,
            type: 'GET',
            dataType: 'json',
            success: function (p) {
                $('#id').val(p.id);
                $('#nome').val(p.nome);
                $('#valor').val(p.valor);
                $('#desconto').val(p.desconto != null ? p.desconto : '');
            },
            error: function () {
                exibirAlerta('danger', 'Produto não encontrado.');
            }
        });
    }

    $('#btn-salvar').on('click', function () {

        var payload = {
            nome:     $('#nome').val().trim(),
            valor:    $('#valor').val().trim(),
            desconto: $('#desconto').val().trim() || null
        };
        var idVal = $('#id').val();
        if (idVal) payload.id = parseInt(idVal, 10);

        $.ajax({
            url: ctxPath + '/api/produto',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            dataType: 'json',
            success: function (resp) {
                if (resp.success) {
                    exibirAlerta('success', resp.message);
                    if (!$('#id').val()) {
                        $('#nome').val('');
                        $('#valor').val('');
                        $('#desconto').val('');
                    }
                } else {
                    exibirAlerta('danger', resp.message);
                }
            },
            error: function (xhr) {
                var msg = 'Erro ao salvar produto.';
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
