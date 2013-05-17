<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require allPrivileges="Manage Cashier Metadata, View Cashier Metadata" otherwise="/login.htm" redirect="/module/openhmis/cashier/paymentModes.form" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>
<openmrs:htmlInclude file="/moduleResources/openhmis/cashier/js/screen/paymentModes.js" />

<h2>
    <spring:message code="openhmis.cashier.admin.paymentModes" />
</h2>