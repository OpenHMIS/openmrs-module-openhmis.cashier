/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and
 * limitations under the License.
 *
 * Copyright (C) OpenHMIS.  All Rights Reserved.
 */
package org.openmrs.module.openhmis.cashier.web.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.jasperreport.JasperReport;
import org.openmrs.module.jasperreport.JasperReportService;
import org.openmrs.module.jasperreport.ReportGenerator;
import org.openmrs.module.jasperreport.util.JasperReportConstants;
import org.openmrs.module.openhmis.cashier.web.CashierWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping(value = CashierWebConstants.JASPER_REPORT_PAGE)
public class JasperReportController {
	@RequestMapping(method = RequestMethod.GET)
	public String render(@RequestParam(value = "reportId", required = true) int reportId, WebRequest request,
	        HttpServletResponse response) throws IOException {
		// Currently we only handle the cashier shift report so this method is tailored to it.
		String message = null;
		int timesheetId;
		String temp = request.getParameter("timesheetId");
		if (!StringUtils.isEmpty(temp) && StringUtils.isNumeric(temp)) {
			timesheetId = Integer.parseInt(temp);
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The timesheet id ('" + temp + "') must be " +
					"defined and be numeric.");
			return null;
		}
		
		JasperReportService jasperService = Context.getService(JasperReportService.class);
		JasperReport report = jasperService.getJasperReport(reportId);
		if (report == null) {
			message = "Could not find report '" + reportId + "'";
			return "redirect:" + JasperReportConstants.REPORT_ERROR_PAGE + "?reportId="+reportId+"&message="+message;
		}
		
		report.setName("Cashier Shift Report - " + temp);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("timesheetId", timesheetId);
		try {
			ReportGenerator.generate(report, params, false, true);
		} catch (IOException e) {
			message = "Error generating cashier shift report for " + "timesheet '" + temp + "'";
			return "redirect:" + JasperReportConstants.REPORT_ERROR_PAGE + "?reportId="+reportId+"&message="+message;
		}
		
		return "redirect:" + CashierWebConstants.REPORT_DOWNLOAD_URL + "?reportName="
		        + report.getName().replaceAll("\\W", "") + ".pdf";
	}
}
