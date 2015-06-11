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

import org.openmrs.api.context.Context;
import org.openmrs.module.jasperreport.JasperReport;
import org.openmrs.module.jasperreport.ReportGenerator;
import org.openmrs.module.openhmis.cashier.ModuleSettings;
import org.openmrs.module.openhmis.cashier.api.IBillService;
import org.openmrs.module.openhmis.cashier.api.model.Bill;
import org.openmrs.module.openhmis.cashier.api.util.PrivilegeConstants;
import org.openmrs.module.openhmis.cashier.web.CashierWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = CashierWebConstants.RECEIPT)
public class ReceiptController {
	@RequestMapping(method = RequestMethod.GET)
	public void get(@RequestParam(value = "receiptNumber", required = false) String receiptNumber,
	        HttpServletResponse response) throws IOException {
		if (receiptNumber == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		IBillService service = Context.getService(IBillService.class);
		Bill bill = service.getBillByReceiptNumber(receiptNumber);
		if (!validateBill(receiptNumber, bill, response)) {
			return;
		}
		
		JasperReport report = ModuleSettings.getReceiptReport();
		if (report == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configuration error: need to specify global " +
					"option for default report ID.");
			return;
		}
		
		if (generateReport(receiptNumber, response, bill, report)) {
			bill.setReceiptPrinted(true);
			service.save(bill);
		}
	}
	
	private boolean generateReport(String receiptNumber, HttpServletResponse response, Bill bill, JasperReport report)
	        throws IOException {
		String name = report.getName();
		report.setName(receiptNumber);
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("billId", bill.getId());
		
		try {
			ReportGenerator.generateHtmlAndWriteToResponse(report, params, response);
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report for receipt '" +
					receiptNumber + "'");
			return false;
		} finally {
			// Reset the report name
			report.setName(name);
		}
		
		return true;
	}
	
	private boolean validateBill(String receiptNumber, Bill bill, HttpServletResponse response) throws IOException {
		if (bill == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find bill with receipt number '" +
					receiptNumber  + "'");

			return false;
		}
		
		if (bill.isReceiptPrinted() && !Context.hasPrivilege(PrivilegeConstants.REPRINT_RECEIPT)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to reprint receipt '" +
					receiptNumber + "'");
			return false;
		}
		
		return true;
	}
}
