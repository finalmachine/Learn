package com.gbi.jsoup.grab;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
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

public class FranceTask1 {

	public static final String entryUrl = "https://www.transparence.sante.gouv.fr/flow/interrogationAvancee?execution=e1s1";
	public static final String entryUrlpre = "https://www.transparence.sante.gouv.fr/flow/interrogationAvancee";
	public static final String restoreTable = "Recherche_avancee2";// TODO

	public static String begin = null;
	public static String end = null;
	public static HashSet<String> store = new HashSet<String>();
	private static boolean normalExit = true;
	private BasicHttpClient client = null;
	private Elements options = null;

	private Cookie cookie = null;
	private Iterator<Element> iterator = null;
	private Element currentOption = null;
	private int loop1 = 0;
	private int loop2 = 0;
	private int count = 0;
	private int re = 0;
	
	private HashSet<Map<String, String>> set = new HashSet<Map<String,String>>();

	private static void printError(SimpleHttpErrorInfo info) {
		// MongoEntityClient mongoClient = new MongoEntityClient();
		normalExit = false;
		System.out.println(info);
	}

	private static void reportError(SimpleHttpErrorInfo info) {
		normalExit = false;
		System.out.println(info);
		JOptionPane.showMessageDialog(null, new JLabel("<html><font face='微软雅黑' size='5' color='red'>"
				+ info.getInfo() + "</font></html>"), "T_T", JOptionPane.ERROR_MESSAGE);
	}

	private void prepareData(HashMap<String, String> data) {
		data.put("form", "form");
		data.put("form:regionEntreprise", "Sélectionner une région");
		data.put("form:departementEntreprise", "Sélectionner un département");// currentOption2.val()
		data.put("form:paysEntreprise", currentOption.val());
		data.put("form:dateDebut", begin);
		data.put("form:dateFin", end);
		data.put("javax.faces.ViewState", "e1s1");
		// 空参数
		data.put("form:minDateAvantage", "");
		data.put("form:maxDateAvantage", "");
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
		data.put("form:denominationSociale-autocomplete", "");
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
		case 1:
			reportError(new SimpleHttpErrorInfo(entryUrl, "首页为null"));
			client.close();
			return;
		case 2:
			break;
		default:
			reportError(new SimpleHttpErrorInfo(entryUrl, "找不到首页了"));
			client.close();
			return;
		}
		Document dom = content.getDocument();
		// 得到要选择的option
		// options = content.getDocument().select(
		// "select[id=form:regionEntreprise]>option:not(:containsOwn(Sélectionner une région))");
		options = dom
				.select("select[id=form:paysEntreprise]>option:not(:containsOwn(Sélectionner un pays))");

		// 得到默认选择的值
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
		// 开始捕获数据
		grabStep1();
	}

	public void grabStep1() {
		// 准备好当前需要的option
		if (iterator == null) {
			iterator = options.iterator();
		}
		if (currentOption == null) {
			if (iterator.hasNext()) {
				currentOption = iterator.next();
			} else {
				currentOption = null;
			}
		}

		while (currentOption != null) {
			// 准备参数
			HashMap<String, String> data = new HashMap<String, String>();
			prepareData(data);
			// 准备URL地址
			cookie = client.getCookieStore().getCookies()
					.get(client.getCookieStore().getCookies().size() - 1);
			String url = entryUrlpre + ";" + cookie.getName().toLowerCase() + "="
					+ cookie.getValue() + "?execution=e1s1";
			System.out.println(url);
			BasicHttpResponse content = null;
			try {
				content = client.post(url, data);// 弱执行成功 content的URL会发生改变
				System.out.println(client.getLastStatus());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("...");
			switch (checkResponse(content, url)) {
			case 1:// TODO
				System.out.println("1.1");
				printError(new SimpleHttpErrorInfo(url, currentOption.text() + "已作废，继续处理后面的事情",
						HttpMethod.POST, data));
				if (iterator.hasNext()) {
					currentOption = iterator.next();
					// changeOption(currentOption);
				} else {
					currentOption = null;
				}
				continue;
			case 2:// TODO
				System.out.println("1.2");
				if (loop1 > 5) {
					reportError(new SimpleHttpErrorInfo(url, "程序从首页出不去了，即将推出", HttpMethod.POST, data));
					client.close();
					System.exit(2);
				} else {
					System.out.println("grab1-loop");
					++loop1;
					continue;
				}
			case 3:
				content = showDialog(content);
				if (content == null) {
					continue;
				}
			case 4:
				grabStep2(content, true, true);
				System.out.println("count:" + count);
				grabStep2(content, false, true);
				System.out.println("count:" + count);

				if (iterator.hasNext()) {
					System.out.println("切换之前：" + currentOption.text());
					currentOption = iterator.next();
					// changeOption(currentOption);
					System.out.println("切换之后：" + currentOption.text());
				} else {
					currentOption = null;
				}
				continue;
			case 5:
				reportError(new SimpleHttpErrorInfo(url, "发生了某些未知的错误"));
				client.close();
				return;
			}
		}
	}

	private void grabStep2(final BasicHttpResponse content, boolean main, boolean firstPage) {

		BasicHttpResponse content1 = content;

		// 如果是标签2 跳转至标签2
		if (main == false && firstPage) {
			Elements inputs = null;
			Element input = null;
			String key1 = null, key2 = null, val1 = null, val2 = null;

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

			try {
				content1 = client.post(content.getUrl(), data);
			} catch (Exception e) {
				if (e instanceof SSLHandshakeException) {
					System.out.println("grab2,SSL连接发生了异常");
				} else {
					System.out.println("发生了未知不到的意外");
					e.printStackTrace();
				}
				System.out.println("<<--grabStep2--" + currentOption.text());
				return;
			}
		}

		// 查看可采集数据是否为0，如果是的，直接结束
		char c = 160;
		int total = Integer.parseInt(content1.getDocument().select("form#j_idt17>fieldset>div>legend>span")
				.first().text().trim().replaceAll("[" + c + "]", ""));
		System.out.println("total:" + total);
		if (total == 0) {
			System.out.println("<--grabStep2--" + currentOption.text());
			return;
		}

		for (int i = 0; i < (total + 19) / 20; ++i) {
			System.out.println("--grabStep2" + currentOption.text() + "--page:"
					+ content1.getDocument().select("li.btn-page>input[disabled]").first().val()
					+ (main ? "--1" : "--2") + "-->");

			// 准备post参数
			Elements inputs = null;
			Element input = null;
			String key1 = null, key2 = null, val1 = null, val2 = null;

			inputs = content1.getDocument().select("form#j_idt17>input");
			if (inputs == null || inputs.size() != 2) {
				reportError(new SimpleHttpErrorInfo(content1.getUrl(), "网页格式变了,我罢工了"));
				return;
			}
			key1 = inputs.get(0).attr("name");
			val1 = inputs.get(0).val();
			key2 = inputs.get(1).attr("name");
			val2 = inputs.get(1).val();
			// 找到对应的table里面的所有input
			if (main) {
				inputs = content1.getDocument().select(
						"table[id=j_idt17:dataTable]>tbody>tr>td:eq(6)>input");
			} else {
				inputs = content1.getDocument().select(
						"table[id=j_idt17:dataTable2]>tbody>tr>td:eq(6)>input");
			}
			if (inputs == null) {
				reportError(new SimpleHttpErrorInfo(content1.getUrl(), "网页格式变了,我罢工了"));
				return;
			}

			// 遍历该页的input 存入数据库
			boolean out = false;
			Iterator<Element> iterator = inputs.iterator();
			input = iterator.next();
			while (input != null) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(key1, val1);
				data.put(key2, val2);
				data.put(input.attr("name"), input.val());
				BasicHttpResponse con = null;
				con = tryToAttach(content1.getUrl(), data);
				switch (checkResponse(con, content1.getUrl())) {
				case 1: // TODO
					System.out.println("case1");
					printError(new SimpleHttpErrorInfo(content1.getUrl(), "中间丢失了一条记录",
							HttpMethod.POST, data));
					if (iterator.hasNext()) {
						input = iterator.next();
						out = false;
					} else {
						input = null;
						out = false;
					}
					continue;
				case 2:// TODO
					reportError(new SimpleHttpErrorInfo(con.getUrl(), "grab2的终止点1",
							HttpMethod.POST, data));
					System.out.println("case2");
					// grabStep1();TODO
					return;
				case 3:// TODO
					showDialog(con);
					System.out.println("case3");
					continue;
				case 4:// TODO
					System.out.println("case4");
					if (loop2 > 5) {
						reportError(new SimpleHttpErrorInfo(con.getUrl(), "我也不知道为什么回来到这里2",
								HttpMethod.POST, data));
						out = true;
						break;
					} else {
						System.out.println("grab2-loop");
						++loop2;
						continue;
					}
				case 5:// TODO
					grabStep3(con);
					if (iterator.hasNext()) {
						input = iterator.next();
						out = false;
					} else {
						input = null;
						out = false;
					}
					continue;
				default:// TODO
					System.out.println("default");
					reportError(new SimpleHttpErrorInfo(con.getUrl(), "我也不知道为什么回来到这里3",
							HttpMethod.POST, data));
					continue;
				}
				if (out) {
					System.out.println("out");
					break;
				}
			}

			// 跳转至下一页
			// 准备post参数
			input = content1.getDocument().select("li.btn-page>input[disabled] + input").first();
			if (input == null) {
				System.out.println("<<<--grabStep2--input-null--" + currentOption.text());
				return;
			} else {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(key1, val1);
				data.put(key2, val2);
			//	data.put(input.attr("name"), input.val());
				data.put(input.attr("name"), "" + (i + 2));
				String url = content1.getUrl();

				boolean in = true;
			//	while (in) {
					/*try {
						content1 = client.post(url, data);
					} catch (Exception e) {
						if (e instanceof SSLHandshakeException) {
							System.out.println("grab 2 page ssl 异常");
						} else {
							try {
								throw e;
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}*/
				while (in) {
					content1 = tryToAttach(url, data);
					switch (checkResponse(content1, url)) {
					case 4:
						in = false;
						break;
					default:
						printError(new SimpleHttpErrorInfo(url, "跳过一页", HttpMethod.POST, data));
						++i;
						if ((i + 2) > ((total + 19) / 20)) {
							System.out.println("<<<--grabStep2--");
							return;
						} else {
							data.put(input.attr("name"), "" + (i + 2));
						}
					}
				}
			}
		}
	}

	private void grabStep3(final BasicHttpResponse content) {
		HashMap<String, String> json = new HashMap<String, String>();
		Elements ps = content.getDocument().select(
				"form#j_idt17>fieldset:eq(3)>div[class=section-content]>p");
		for (Element p : ps) {
			json.put(p.select(">label").first().text(), p.select(">input").first().val());
		}
		ps = content.getDocument().select("form#j_idt17>fieldset:eq(4)>div[class=section-content]>p");
		for (Element p : ps) {
			if (p.select(">input").first() == null) {
				continue;
			}
			json.put(p.select(">label").first().text(), p.select(">input").first().val());
		}
		ps = content.getDocument().select("form#j_idt17>fieldset:eq(5)>div[class=section-content]>p");
		for (Element p : ps) {
			json.put(p.select(">label").first().text(), p.select(">input").first().val());
		}

		if (set.add(json)) {
			System.out.println(++count + "," + re);
		} else {
			++re;
			System.out.println("重复一条数据");
		}
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
						System.out.println("response:" + client.getLastStatus() +"\n" + response);
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
						System.out.println("response:" + client.getLastStatus() +"\n" + response);
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
	public static int checkResponse(BasicHttpResponse content, String targetUrl) {
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
		FranceTask1 frame = new FranceTask1();
		frame.run();
		if (normalExit) {
			System.out.println("normal");
		} else {
			System.out.println("interupt");
		}
	}
}