<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TUCC - Sistema Legado</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style>
        body { padding-top: 60px; background-color: #f5f5f5; }
        .navbar-brand { font-weight: bold; letter-spacing: 1px; }
        .page-header { border-bottom: 2px solid #ddd; margin-bottom: 20px; }
        .table > thead > tr > th { background-color: #e8e8e8; }
        .footer-legado { margin-top: 40px; padding: 15px 0; border-top: 1px solid #ddd;
                         color: #999; font-size: 12px; text-align: center; }
        .label-legado { font-size: 9px; vertical-align: middle; margin-left: 4px; }
    </style>
</head>
<body>

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#navbar-menu">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="${pageContext.request.contextPath}/produto/lista.jsp">
                TUCC
                <span class="label label-warning label-legado">LEGADO</span>
            </a>
        </div>
        <div id="navbar-menu" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li class="${pageContext.request.requestURI.contains('/produto') and not pageContext.request.requestURI.contains('produto_empresa') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/produto/lista.jsp">
                        <span class="glyphicon glyphicon-tag"></span> Produtos
                    </a>
                </li>
                <li class="${pageContext.request.requestURI.contains('/empresa') and not pageContext.request.requestURI.contains('produto') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/empresa/lista.jsp">
                        <span class="glyphicon glyphicon-briefcase"></span> Empresas
                    </a>
                </li>
                <li class="${pageContext.request.requestURI.contains('produto_empresa') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/produto_empresa/lista.jsp">
                        <span class="glyphicon glyphicon-link"></span> Produto-Empresa
                    </a>
                </li>
            </ul>
            <p class="navbar-text navbar-right" style="font-size:11px; color:#aaa;">
                Java 8 &bull; Servlet &bull; Oracle
            </p>
        </div>
    </div>
</nav>

<div class="container">

    <div id="alerts"></div>
