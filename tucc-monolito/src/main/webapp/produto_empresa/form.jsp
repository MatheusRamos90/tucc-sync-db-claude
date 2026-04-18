<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-link"></span> Nova Associação Produto-Empresa
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body">
        <form id="form-associacao" class="form-horizontal">

            <div class="form-group">
                <label class="col-sm-2 control-label">Produto <span class="text-danger">*</span></label>
                <div class="col-sm-5">
                    <select id="produtoId" class="form-control">
                        <option value="">-- Selecione um produto --</option>
                    </select>
                    <span id="aviso-produto" class="help-block text-warning" style="display:none">
                        Nenhum produto cadastrado.
                        <a href="${pageContext.request.contextPath}/produto/form.jsp">Cadastrar produto</a>
                    </span>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Empresa <span class="text-danger">*</span></label>
                <div class="col-sm-5">
                    <select id="empresaId" class="form-control">
                        <option value="">-- Selecione uma empresa --</option>
                    </select>
                    <span id="aviso-empresa" class="help-block text-warning" style="display:none">
                        Nenhuma empresa cadastrada.
                        <a href="${pageContext.request.contextPath}/empresa/form.jsp">Cadastrar empresa</a>
                    </span>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-6">
                    <button type="button" id="btn-salvar" class="btn btn-primary" disabled>
                        <span class="glyphicon glyphicon-floppy-disk"></span> Cadastrar
                    </button>
                    <a href="${pageContext.request.contextPath}/produto_empresa/lista.jsp"
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

    $.ajax({
        url: ctxPath + '/api/produto-empresa?options=true',
        type: 'GET',
        dataType: 'json',
        success: function (data) {
            var selProduto = $('#produtoId');
            var selEmpresa = $('#empresaId');

            if (data.produtos.length === 0) {
                $('#aviso-produto').show();
            } else {
                $.each(data.produtos, function (i, p) {
                    selProduto.append('<option value="' + p.id + '">#' + p.id + ' — ' + escapeHtml(p.nome) + '</option>');
                });
            }

            if (data.empresas.length === 0) {
                $('#aviso-empresa').show();
            } else {
                $.each(data.empresas, function (i, e) {
                    selEmpresa.append('<option value="' + e.id + '">#' + e.id + ' — ' + escapeHtml(e.nome) + '</option>');
                });
            }

            if (data.produtos.length > 0 && data.empresas.length > 0) {
                $('#btn-salvar').prop('disabled', false);
            }
        },
        error: function () {
            exibirAlerta('danger', 'Erro ao carregar opções.');
        }
    });

    $('#btn-salvar').on('click', function () {

        var produtoId = $('#produtoId').val();
        var empresaId = $('#empresaId').val();

        if (!produtoId) { exibirAlerta('danger', 'Selecione um produto.'); return; }
        if (!empresaId) { exibirAlerta('danger', 'Selecione uma empresa.'); return; }

        $.ajax({
            url: ctxPath + '/api/produto-empresa',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ produtoId: parseInt(produtoId, 10), empresaId: parseInt(empresaId, 10) }),
            dataType: 'json',
            success: function (resp) {
                if (resp.success) {
                    exibirAlerta('success', resp.message);
                    $('#produtoId').val('');
                    $('#empresaId').val('');
                } else {
                    exibirAlerta('danger', resp.message);
                }
            },
            error: function (xhr) {
                var msg = 'Erro ao salvar associação.';
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

    function escapeHtml(str) { return $('<div>').text(str || '').html(); }
});
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
