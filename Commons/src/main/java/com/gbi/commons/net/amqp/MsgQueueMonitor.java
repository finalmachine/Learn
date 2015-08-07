package com.gbi.commons.net.amqp;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import com.gbi.commons.net.http.BasicHttpClient;

public class MsgQueueMonitor {

	private String _hostName;
	private String _queueName;
	private List<MsgBase> _subjectList = new ArrayList<>();

	public MsgQueueMonitor(String hostName, String queueName) {
		_hostName = hostName;
		_queueName = queueName;
	}

	public void addSubject(MsgBase... subjects) {
		for (MsgBase subject : subjects) {
			_subjectList.add(subject);
		}
	}

	public void checkFinish() {
		// wait the web update >
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// wait the web update <
		BasicHttpClient client = new BasicHttpClient();
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Authorization", "Basic Z3Vlc3Q6Z3Vlc3Q=");
		try {
			String url = "http://localhost:15672/api/queues/" + URLEncoder.encode(_hostName, "UTF-8") + "/"
					+ URLEncoder.encode(_queueName, "UTF-8");
			TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			int messageNum = -1;
			while (true) {
				JSONObject json = new JSONObject(new String(client.get(url, extraHeaders).getContent()));
				int num = json.getInt("messages");
				if (num == 0) {
					for (MsgBase subject : _subjectList) {
						subject.close();
					}
					break;
				} else {
					if (num != messageNum) {
						Date date = Calendar.getInstance().getTime();
						System.out.println(format.format(date) + ">" + "还有 " + num + " 个消息未处理");
						messageNum = num;
					}
				}
				Thread.sleep(30000);
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
}
