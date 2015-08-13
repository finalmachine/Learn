package com.gbi.jsoup.grab;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.http.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gbi.commons.util.gui.IdentifyingCodeDialog;
import com.gbi.commons.model.SimpleHttpErrorInfo;
import com.gbi.commons.net.http.HttpMethod;
import com.gbi.commons.net.http.BasicHttpClient;
import com.gbi.commons.net.http.BasicHttpResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class FranceTask3 {

	private static final String entryUrl = "https://www.transparence.sante.gouv.fr/flow/interrogationAvancee?execution=e1s1";
	private static final String entryUrlpre = "https://www.transparence.sante.gouv.fr/flow/interrogationAvancee";
	private static final String recordTable = "Payment_France_record";// 记录任务的
	private static final String restoreTable = "Payment_France";// 存储数据的
	private static final String mongoAddr = "127.0.0.1";
	private static final int mongoPort = 27017;

	private static DBCollection collection1 = null; // 记录任务的
	private static DBCollection collection2 = null; // 存储数据的
	private static DBObject task = null;

	private static String begin = null; // 开始的季度
	private static String end = null; // 结束的季度
	private static String url; // 记录首页的含cookie参数的地址，定期访问使得首尔不会过期
	private static BasicHttpClient client = null;
	private static Cookie cookie = null;
	private static Iterator<Element> iterator = null;

	private static Element currentOption = null;
	private static Calendar cal1 = Calendar.getInstance();
	private static Calendar cal2 = Calendar.getInstance();
	private static Calendar endTime = Calendar.getInstance(); // 抓捕数据的最大日期
	private static int page = 1; // 记录当前带读取的页面

	private static boolean normalExit = true; // 表示程序是否完美结束
	private static SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

	private static int loop2 = 0;
	private static int count = 0; // 单次执行存入数据库的数据条数
	private static int re = 0; // 单次执行存入数据库时重复的条数

	private static void printError(SimpleHttpErrorInfo info) {
		System.out.println(info);
	}

	private static void reportError(SimpleHttpErrorInfo info) {
		normalExit = false;
		System.out.println(info);
		JOptionPane.showMessageDialog(null,
				new JLabel("<html><font face='微软雅黑' size='5' color='red'>" + info.getInfo() + "</font></html>"), "T_T",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	private boolean timeBefore(Calendar c1, Calendar c2) {
		if (c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR)) {
			return true;
		} else if (c1.get(Calendar.MONTH) < c2.get(Calendar.MONTH)) {
			return true;
		} else if (c1.get(Calendar.DAY_OF_MONTH) < c2.get(Calendar.DAY_OF_MONTH)) {
			return true;
		}
		return false;
	}

	private void prepareData(HashMap<String, String> data) {
		data.put("form", "form");
		data.put("form:denominationSociale-autocomplete",
				"MERCK MEDICATION FAMILIALE ; MERCK SANTE ; MERCK SERONO ; ");
			//	"PFIZER INTERNATIONAL OPERATIONS ; PFIZER SANTE FAMILIALE ; PFIZER SAS ; ");// Astrazeneca
		data.put("form:regionEntreprise", "Sélectionner une région");
		data.put("form:departementEntreprise", "Sélectionner un département");// currentOption2.val()
		data.put("form:paysEntreprise", currentOption.val());
		data.put("form:dateDebut", begin);
		data.put("form:dateFin", end);
		data.put("javax.faces.ViewState", "e1s1");
		data.put("form:minDateAvantage", format.format(cal1.getTime()));
		data.put("form:maxDateAvantage", format.format(cal2.getTime()));
		// 空参数
		data.put("form:dateAvantage", "");
		data.put("form:minMontantAvantage", "");
		data.put("form:maxMontantAvantage", "");
		data.put("form:montantAvantage", "");
		data.put("form:natureAvantage", "");
		data.put("form:referenceConvention", "");
		data.put("form:dateConventionEnCours", "");
		data.put("form:nomManifestationConvention", "");
		data.put("form:dateManifestationConvention", "");
		data.put("form:lieuManifestationConvention", "");
		data.put("form:organisateurManifestationConvention", "");
		data.put("form:objetCategorielConvention", "");
		data.put("form:numeroSirenEntreprise", "");
		data.put("form:codePostalEntreprise", "");
		data.put("form:villeEntreprise", "");
		data.put("form:identifiantBeneficiaire", "");
		data.put("form:nom-autocomplete", "");
		data.put("form:codePostalBeneficiaire", "");
		data.put("form:villeBeneficiaire", "");
		data.put("form:nomEtablissementBeneficiaire", "");
		// 默认参数
		data.put("form:qualiteBeneficiaire", "Toutes");
		data.put("form:specialiteBeneficiaire", "Toutes");
		data.put("form:categorieBeneficiaire", "Toutes");
		data.put("form:typeIdentifiantBeneficiaire", "Tous");
		data.put("form:j_idt185", "Rechercher");
		data.put("form:regionBeneficiaire", "Sélectionner une région");
		data.put("form:departementBeneficiaire", "Sélectionner un département");
	}

	private void run() throws IOException {
		client = new BasicHttpClient();
		BasicHttpResponse content = null;
		content = tryToAttach(entryUrl, null);
		switch (checkResponse(content, entryUrl)) {
		case 2:
			break;
		default:
			reportError(new SimpleHttpErrorInfo(entryUrl, "找不到首页了"));
			client.close();
			return;
		}
		Document dom = content.getDocument();
		Elements options = dom.select("select[id=form:paysEntreprise]>option:not(:containsOwn(Sélectionner un pays))");

		// 得到季度时间区间
		Element option1 = content.getDocument().select("select[id=form:dateDebut]>option:eq(0)").first();
		Element option2 = content.getDocument().select("select[id=form:dateFin]>option:eq(0)").first();
		// 监察网页数据是否符合预期
		if (options == null || options.size() == 0 || option1 == null || option2 == null) {
			reportError(new SimpleHttpErrorInfo(entryUrl, "不是你的错，网页变化了"));
			client.close();
			return;
		}
		begin = option1.val();
		end = option2.val();
		endTime.set(2015, 6, 1);// TODO
		// 初始化iterator
		iterator = options.iterator();

		// get value from mongo >
		MongoClient client = new MongoClient(mongoAddr, mongoPort);
		DB db = client.getDB("NAVISUS");
		collection1 = db.getCollection(recordTable);
		collection2 = db.getCollection(restoreTable);
		DBObject query = new BasicDBObject();
		query.put("status", "begin");
		task = collection1.findOne(query);
		if (task == null) { // 没有需要执行的任务
			task = new BasicDBObject();
			task.put("status", "begin");
			task.put("startTime", new Date());
			if (iterator.hasNext()) {
				currentOption = iterator.next();
			} else {
				currentOption = null;
			}
			task.put("option", currentOption.text());
			task.put("page", 1);
			cal1.set(2012, 5, 30);// TODO
			cal2.set(2012, 6, 1);// TODO
			task.put("date1", cal1.getTime());
			task.put("date2", cal2.getTime());
			collection1.save(task);
		} else { // 有需要执行的任务
			String optionName = (String) task.get("option");
			while (iterator.hasNext()) {
				Element element = iterator.next();
				if (element.text().equals(optionName)) {
					currentOption = element;
					break;
				}
			}
			page = (Integer) task.get("page");
			cal1.setTime((Date) task.get("date1"));
			cal2.setTime((Date) task.get("date2"));
		}
		// get value from mongo <
		// begin grab
		grabStep1();
	}

	private void grabStep1() {
		while (currentOption != null) {
			// 准备参数
			HashMap<String, String> data = new HashMap<String, String>();
			prepareData(data);
			// 准备URL地址
			List<Cookie> cookies = client.getCookieStore().getCookies();
			cookie = cookies.get(cookies.size() - 1);
			url = entryUrlpre + ";" + cookie.getName().toLowerCase() + "=" + cookie.getValue() + "?execution=e1s1";
			BasicHttpResponse content = tryToAttach(url, data);
			System.out.println("试图更新");
			switch (checkResponse(content, url)) {
			case 2:
				System.out.println(content.getDocument());
				continue;
			case 3:
				content = showDialog(content);
				if (content == null) {
					System.out.println("再次输入校验码");
					continue;
				}
			case 4:
				grabStep2(content, true, true);
				System.out.println("count:" + count);
				page = 1;
				// grabStep2(content, false, true);TODO
				// System.out.println("count:" + count);TODO
				// page = 1;TODO
				tryToAttach(url, null); // 保持首页不过期
				cal1.add(Calendar.DAY_OF_MONTH, 2); // 日期向后两天
				cal2.add(Calendar.DAY_OF_MONTH, 2);
				if (!timeBefore(cal2, endTime)) {
					if (iterator.hasNext()) {
						currentOption = iterator.next();
						cal1.set(2012, 5, 30);// TODO
						cal2.set(2012, 6, 1);// TODO
						task.put("option", currentOption.text());
						System.out.println("切换之后：" + currentOption.text());
					} else {
						currentOption = null;
						task.put("status", "end");
						task.put("endTime", new Date());
					}
				}
				task.put("page", page);
				task.put("date1", cal1.getTime());
				task.put("date2", cal2.getTime());
				collection1.save(task);
				continue;
			default:
				System.out.println(checkResponse(content, url));
				reportError(new SimpleHttpErrorInfo(url, "发生了某些未知的错误", HttpMethod.POST, data));
				client.close();
				return;
			}
		}
	}

	private void grabStep2(final BasicHttpResponse content, boolean main, boolean firstPage) {
		BasicHttpResponse content1 = content;// 拷贝content指针
		// 准备存储网页数据的空间
		Elements inputs = null;
		Element input = null;
		String key1 = null, key2 = null, val1 = null, val2 = null;

		// 如果是标签2 跳转至标签2 >
		if (main == false && firstPage) {
			inputs = content.getDocument().select("form#j_idt17>input");
			if (inputs == null || inputs.size() != 2) {
				printError(new SimpleHttpErrorInfo(content.getUrl(), "跳转至标签2：错误1"));
				System.out.println("<<--grabStep2--" + currentOption.text());
				return;
			}
			key1 = inputs.get(0).attr("name");
			val1 = inputs.get(0).val();
			key2 = inputs.get(1).attr("name");
			val2 = inputs.get(1).val();
			input = content.getDocument().select("form#j_idt17>ul>li:eq(1)>input").first();
			if (input == null) {
				printError(new SimpleHttpErrorInfo(content.getUrl(), "跳转至标签2：错误2"));
				System.out.println("<<--grabStep2--" + currentOption.text());
				return;
			}
			HashMap<String, String> data = new HashMap<String, String>();
			data.put(key1, val1);
			data.put(key2, val2);
			data.put(input.attr("name"), input.val());
			loop2 = 0;
			while (loop2 < 5) {
				content1 = tryToAttach(content.getUrl(), data);
				switch (checkResponse(content1, content.getUrl())) {
				case 4:
					loop2 = 8;
					break;
				default:
					System.out.println("jump err");
					++loop2;
					continue;
				}
			}
			if (loop2 == 5) {
				reportError(new SimpleHttpErrorInfo(content.getUrl(), "跳转至标签2失败", HttpMethod.POST, data));
				return;
			}
		} // 如果是标签2 跳转至标签2 <

		// 查看可采集数据是否为0 如果是的 直接结束 >
		char c = 160;
		String temp = content1.getDocument().select("form#j_idt17>fieldset>div>legend>span").first()
				.text().trim().replaceAll("[" + c + "]", "").replaceAll("[,]", "");
		int total = Integer.parseInt(temp);
		System.out.println("total:" + total);
		if (total == 0) {
			System.out.println("<--grabStep2--" + "-" + currentOption.text() + "-" + format.format(cal1.getTime())
					+ (main ? "--1" : "--2"));
			return;
		}
		// 查看可采集数据是否为0 如果是的 直接结束 <

		// 查看page与网页上的页码是否一致 >
		if (Integer.parseInt(content1.getDocument().select("li.btn-page>input[disabled]").first().val()) != page) {
			System.out.println("page:" + page);
			System.out.println("real:" + content1.getDocument().select("li.btn-page>input[disabled]").first().val());
			inputs = content1.getDocument().select("form#j_idt17>input");
			if (inputs == null || inputs.size() != 2) {
				reportError(new SimpleHttpErrorInfo(content1.getUrl(), "网页格式变了,我罢工了"));
				return;
			}

			key1 = inputs.get(0).attr("name");
			val1 = inputs.get(0).val();
			key2 = inputs.get(1).attr("name");
			val2 = inputs.get(1).val();

			String url = content1.getUrl();
			loop2 = 0;
			int pageNow = 1;
			HashMap<String, String> data = null;
			while (loop2 < 5 && pageNow < page) {
				data = new HashMap<String, String>();
				data.put(key1, val1);
				data.put(key2, val2);
				input = content1.getDocument().select("li.btn-page>input").last();
				if (page > Integer.parseInt(input.val())) {
					data.put(input.attr("name"), input.val());
				} else {
					String select = "li.btn-page>input[disabled]";
					for (int i = 0; i < page - pageNow; ++i) {
						select += " + input";
					}
					input = content1.getDocument().select(select).first();
					data.put(input.attr("name"), input.val());
				}
				BasicHttpResponse content2 = content1;
				content1 = tryToAttach(url, data);
				switch (checkResponse(content1, url)) {
				case 4:
					pageNow = Integer
							.parseInt(content1.getDocument().select("li.btn-page>input[disabled]").first().val());
					loop2 = 0;
					break;
				default:
					System.out.println("jump err");
					content1 = content2;
					++loop2;
					continue;
				}
			}
			if (loop2 == 5) {
				reportError(new SimpleHttpErrorInfo(content.getUrl(), "跳转至指定的页码失败", HttpMethod.POST, data));
				return;
			}
		}
		// 查看page与网页上的页码是否一致 <

		// 循环捕获所有的页面的input >
		while (page <= (total + 19) / 20) {
			System.out.println("--grabStep2" + "-" + currentOption.text() + "-" + format.format(cal1.getTime())
					+ "--page:" + content1.getDocument().select("li.btn-page>input[disabled]").first().val()
					+ (main ? "--1" : "--2") + "-->");

			inputs = content1.getDocument().select("form#j_idt17>input");
			if (inputs == null || inputs.size() != 2) {
				reportError(new SimpleHttpErrorInfo(content1.getUrl(), "网页格式变了,我罢工了"));
				return;
			}
			key1 = inputs.get(0).attr("name");
			val1 = inputs.get(0).val();
			key2 = inputs.get(1).attr("name");
			val2 = inputs.get(1).val();
			// 找到对应的table里面的所有input >
			if (main) {
				inputs = content1.getDocument().select("table[id=j_idt17:dataTable]>tbody>tr>td:eq(6)>input");
			} else {
				inputs = content1.getDocument().select("table[id=j_idt17:dataTable2]>tbody>tr>td:eq(6)>input");
			}
			if (inputs == null) {
				reportError(new SimpleHttpErrorInfo(content1.getUrl(), "网页格式变了,我罢工了"));
				return;
			}
			// 找到对应的table里面的所有input <

			// 遍历该页的input存入数据库 >
			Iterator<Element> iterator = inputs.iterator();
			input = iterator.next();
			while (input != null) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(key1, val1);
				data.put(key2, val2);
				data.put(input.attr("name"), input.val());
				loop2 = 0;
				while (loop2 < 5) {
					BasicHttpResponse con = tryToAttach(content1.getUrl(), data);
					switch (checkResponse(con, content1.getUrl())) {
					case 5:
						grabStep3(con);
						if (iterator.hasNext()) {
							input = iterator.next();
						} else {
							input = null;
						}
						loop2 = 8;
						break;
					default:
						System.out.println("default");
						printError(new SimpleHttpErrorInfo(content1.getUrl(), "我也不知道为什么会来到这里", HttpMethod.POST, data));
						++loop2;
						break;
					}
				}
				if (loop2 == 5) {
					reportError(new SimpleHttpErrorInfo(content1.getUrl(), "有一个input访问不到", HttpMethod.POST, data));
				}
			}
			// 遍历该页的input存入数据库 <

			++page;
			task.put("page", page);
			collection1.save(task);
			System.out.println("....");
			tryToAttach(url, null); // 首页保持不过期
			// 跳转至下一页
			// 准备post参数
			input = content1.getDocument().select("li.btn-page>input[disabled] + input").first();
			if (input == null) {
				System.out.println("<<<--grabStep2-input-null--" + currentOption.text());
				return;
			} else {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(key1, val1);
				data.put(key2, val2);
				data.put(input.attr("name"), "" + page);
				String url = content1.getUrl();
				loop2 = 0;
				while (loop2 < 5) {
					content1 = tryToAttach(url, data);
					switch (checkResponse(content1, url)) {
					case 4:
						loop2 = 8;
						break;
					default:
						++loop2;
						System.out.println("再次尝试连接下一页");
						break;
					}
				}
				if (loop2 == 5) {
					reportError(new SimpleHttpErrorInfo(url, "连接不到下一页", HttpMethod.POST, data));
					return;
				}
			}
		}
	}

	private void grabStep3(final BasicHttpResponse content) {
		DBObject json = new BasicDBObject();
		DBObject entreprise = new BasicDBObject();
		Elements ps = content.getDocument().select("form#j_idt17>fieldset:eq(3)>div[class=section-content]>p");
		for (Element p : ps) {
			entreprise.put(p.select(">label").first().text(), p.select(">input").first().val());
		}
		DBObject bénéficiaire = new BasicDBObject();
		ps = content.getDocument().select("form#j_idt17>fieldset:eq(4)>div[class=section-content]>p");
		for (Element p : ps) {
			if (p.select(">input").first() == null) {
				continue;
			}
			bénéficiaire.put(p.select(">label").first().text(), p.select(">input").first().val());
		}
		DBObject avantage = new BasicDBObject();
		ps = content.getDocument().select("form#j_idt17>fieldset:eq(5)>div[class=section-content]>p");
		for (Element p : ps) {
			avantage.put(p.select(">label").first().text(), p.select(">input").first().val());
		}
		json.put("_id", avantage.get("Identifiant"));
		json.put("entreprise", entreprise);
		json.put("bénéficiaire", bénéficiaire);
		json.put("avantage", avantage);
		json.put("html", content.getDocument().toString());
		if (collection2.save(json).isUpdateOfExisting()) {
			++re;
			System.out.println("一共重复：" + re);
		} else {
			// System.out.println(++count + "," + re);
			++count;
		}
		// 循环捕获所有的页面的input <
	}

	private BasicHttpResponse tryToAttach(String url, Map<String, String> data) {
		int times = 0;
		if (data == null) {
			while (times < 10) {
				try {
					BasicHttpResponse response = client.get(url, false);
					if (client.getLastStatus() == 200) {
						return response;
					} else {
						System.out.println("response:" + client.getLastStatus());
					}
				} catch (Exception e) {
					System.err.println("-------------------");
					e.printStackTrace();
					System.err.println("-------------------");
				}
				++times;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.out.println("sleep error");
				}
			}
		} else {
			while (times < 10) {
				try {
					BasicHttpResponse response = client.post(url, data, false);
					if (client.getLastStatus() == 200) {
						return response;
					} else {
						System.out.println("response:" + client.getLastStatus());
					}
				} catch (Exception e) {
					System.err.println("-------------------");
					e.printStackTrace();
					System.err.println("-------------------");
				}
				++times;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.out.println("sleep error");
				}
			}
		}
		if (times == 10) {
			System.out.println("10次都没成功");
		} else {
			System.out.println("不是10次：" + times);
		}
		return null;
	}

	/**
	 * 
	 * @param content
	 * @return null if not need response
	 */
	private BasicHttpResponse showDialog(final BasicHttpResponse content) {
		Element img = content.getDocument().select("div.section-content>img").first();
		if (img == null) {
			reportError(new SimpleHttpErrorInfo(content.getUrl(), "找不到验证码，蛋疼啊"));
			return null;
		}
		BasicHttpResponse con = null;
		try {
			con = client.get(img.absUrl("src"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (con == null) {
			reportError(new SimpleHttpErrorInfo(img.absUrl("src"), "找不到验证图片，网速不好"));
			return null;
		}
		ImageIcon image = new ImageIcon(con.getContent());
		String text = new IdentifyingCodeDialog(image).showDialog();

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("j_idt22", "j_idt22");
		data.put("j_idt22:j_captcha_response", text);
		data.put("j_idt22:j_idt42", "Valider");
		data.put("javax.faces.ViewState", "e1s2");
		try {
			con = client.post(content.getUrl(), data);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (checkResponse(con, content.getUrl()) == 4) {
			return con;
		} else {
			System.out.println("验证码提交之后没有得到相应的结果");
			return null;
		}
	}

	/**
	 * @param content
	 * @param targetUrl
	 * @return 1 空 2 选项网页 3 验证码网页，内部已经刷新验证码 4 捕获数据的2级网页 5 捕获数据的3级网页 6未知的网页
	 */
	private static int checkResponse(BasicHttpResponse content, String targetUrl) {
		if (content == null) {
			return 1;
		} else if (content.getDocument().select("select[id=form:regionEntreprise]>option").size() > 0) {
			return 2;
		} else if (content.getDocument().select("div.section-content>img").size() > 0) {
			return 3;
		} else if (content.getDocument().select("form#j_idt17>fieldset>div>legend>span").size() > 0) {
			return 4;
		} else if (content.getDocument().select("form#j_idt17>fieldset").size() == 3) {
			return 5;
		} else {
			reportError(new SimpleHttpErrorInfo(content.getUrl(), "我迷路了，快找程序员哥哥救我"));
			return 6;
		}
	}

	public static void main(String[] args) throws IOException {
		FranceTask3 frame = new FranceTask3();
		frame.run();
		if (normalExit) {
			System.out.println("normal");
		} else {
			System.out.println("interupt");
		}
		// MERCK MEDICATION FAMILIALE ; MERCK SANTE ; MERCK SERONO ;
	}
}