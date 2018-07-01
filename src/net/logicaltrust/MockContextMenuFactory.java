package net.logicaltrust;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import burp.IContextMenuFactory;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import net.logicaltrust.model.MockEntry;
import net.logicaltrust.model.MockRule;
import net.logicaltrust.persistent.MockAdder;

public class MockContextMenuFactory implements IContextMenuFactory, ActionListener {

	private SimpleLogger logger;
	private IContextMenuInvocation invocation;
	private IExtensionHelpers helpers;
	private MockAdder mockAdder;
	
	public MockContextMenuFactory(SimpleLogger logger, IExtensionHelpers helpers, MockAdder mockAdder) {
		this.logger = logger;
		this.helpers = helpers;
		this.mockAdder = mockAdder;
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		this.invocation = invocation;
		JMenuItem jMenuItem = new JMenuItem("Mock HTTP response");
		jMenuItem.addActionListener(this);
		List<JMenuItem> list = new ArrayList<>();
		list.add(jMenuItem);
		return list;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			IHttpRequestResponse[] selectedMessages = invocation.getSelectedMessages();
			
			if (selectedMessages == null) {
				logger.debug("No selected messages");
				return;
			}
			
			for (IHttpRequestResponse msg : selectedMessages) {
				IRequestInfo analyzedReq = helpers.analyzeRequest(msg.getHttpService(), msg.getRequest());
				URL analyzedURL = analyzedReq.getUrl();
				MockRule mockRule = new MockRule(analyzedURL);
				MockEntry mockEntry = new MockEntry(true, mockRule, msg.getResponse());
				mockAdder.addMock(mockEntry);
				logger.debug("Mock added for " + mockRule);
			}
		} catch (Exception ex) {
			logger.getStderr().println("Cannot mock messages");
			ex.printStackTrace(logger.getStderr());
		}
	}

}