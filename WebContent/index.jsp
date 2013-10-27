<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Currency Converter</title>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>

		<script type="text/javascript">
			$(function() {
				function updateExchangeResult(data, textStatus, jqXHR) {
					$('#exchangeResult').text(data)
				}
				
				function getExchangeResult(event) {
					$.ajax({
						url: 'Main',
						type: 'POST',
						beforeSend: function(jqXHR, settings) {
							if(($('#currencyValue').val() <= 0.00) ||
								$('#fromCurrencyType option:selected').val() == $('#toCurrencyType option:selected').val()) {
								return false;
							}
						},
						data: { 
							type: 'convert',
							currencyValue: $('#currencyValue').val(),
							fromCurrencyType: $('#fromCurrencyType option:selected').val(),
							toCurrencyType: $('#toCurrencyType option:selected').val()					
						},
						success: updateExchangeResult,
						failure: updateExchangeResult
					});	
				}

				$('#currencyValue').on('keypress', getExchangeResult);
				$('#fromCurrencyType').on('change', getExchangeResult);
				$('#toCurrencyType').on('change', getExchangeResult);
			});
		</script>
	</head>

	<body>
		<form id="formExchangeRate" onsubmit="javascript:return false;">
			<input type="text" id="currencyValue" size="10" value="0.00">
			<select name="fromCurrencyType" id="fromCurrencyType">
				<c:forEach items="${currencies}" var="currency">
					<option value="${currency}">${currency}</option>
				</c:forEach>
			</select>
			<span>-&gt;</span>
			<select name="toCurrencyType" id="toCurrencyType">
				<c:forEach items="${currencies}" var="currency">
					<option value="${currency}">${currency}</option>
				</c:forEach>
			</select>
		</form>
		
		<div id="exchangeResult"></div>
	</body>
</html>