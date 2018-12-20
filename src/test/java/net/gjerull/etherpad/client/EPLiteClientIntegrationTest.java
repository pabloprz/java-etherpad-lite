package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
	private EPLiteClient client;
	private ClientAndServer mockServer;
	private static EtmMonitor monitor;

	@Before
	public void startMockServer() {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");

		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.mockserver.mock"))
				.setLevel(ch.qos.logback.classic.Level.OFF);

		mockServer = startClientAndServer(9001);

		BasicEtmConfigurator.configure();
		monitor = EtmManager.getEtmMonitor();
		monitor.start();
	}

	@After
	public void stopMockServer() {
		mockServer.stop();

		monitor.render(new SimpleTextRenderer());
		monitor.stop();
	}

	@Test
	public void validate_token() throws Exception {

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.checkToken();
	}

//	[2018-11-15 18:36:01.542] [INFO] API - REQUEST, v1.2.13:createGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	[2018-11-15 18:36:01.545] [INFO] API - RESPONSE, createGroup, {"code":0,"message":"ok","data":{"groupID":"g.MAPOoRpxOr5vYwkY"}}
//	[2018-11-15 18:36:01.565] [INFO] API - REQUEST, v1.2.13:deleteGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.MAPOoRpxOr5vYwkY"}
//	[2018-11-15 18:36:01.567] [INFO] API - RESPONSE, deleteGroup, {"code":0,"message":"ok","data":null}

	@Test
	public void create_and_delete_group() throws Exception {

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.MAPOoRpxOr5vYwkY\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.MAPOoRpxOr5vYwkY")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

		client.deleteGroup(groupId);
	}

//	REQUEST, v1.2.13:createGroupIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupMapper":"groupname"}
//	RESPONSE, createGroupIfNotExistsFor, {"code":0,"message":"ok","data":{"groupID":"g.P4hyXp9EkkQfRN0r"}}
//	REQUEST, v1.2.13:listAllGroups, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	RESPONSE, listAllGroups, {"code":0,"message":"ok","data":{"groupIDs":["g.P4hyXp9EkkQfRN0r"]}}
//	REQUEST, v1.2.13:createGroupIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupMapper":"groupname"}
//	RESPONSE, createGroupIfNotExistsFor, {"code":0,"message":"ok","data":{"groupID":"g.P4hyXp9EkkQfRN0r"}}
//	REQUEST, v1.2.13:listAllGroups, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	RESPONSE, listAllGroups, {"code":0,"message":"ok","data":{"groupIDs":["g.P4hyXp9EkkQfRN0r"]}}
//	REQUEST, v1.2.13:deleteGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.P4hyXp9EkkQfRN0r"}
//	RESPONSE, deleteGroup, {"code":0,"message":"ok","data":null}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
		String groupMapper = "groupname";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.P4hyXp9EkkQfRN0r\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.P4hyXp9EkkQfRN0r\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.P4hyXp9EkkQfRN0r")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		try {
			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			client.createGroupIfNotExistsFor(groupMapper);

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			client.deleteGroup(groupId);
		}
	}

//	REQUEST, v1.2.13:createGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	RESPONSE, createGroup, {"code":0,"message":"ok","data":{"groupID":"g.ZlouqmXZVtoTu2pf"}}
//	REQUEST, v1.2.13:createGroupPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.ZlouqmXZVtoTu2pf","padName":"integration-test-1"}
//	RESPONSE, createGroupPad, {"code":0,"message":"ok","data":{"padID":"g.ZlouqmXZVtoTu2pf$integration-test-1"}}
//	[REQUEST, v1.2.13:setPublicStatus, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"g.ZlouqmXZVtoTu2pf$integration-test-1","publicStatus":"true"}
//	RESPONSE, setPublicStatus, {"code":0,"message":"ok","data":null}

//	REQUEST, v1.2.13:getPublicStatus, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"g.ZlouqmXZVtoTu2pf$integration-test-1"}
//	RESPONSE, getPublicStatus, {"code":0,"message":"ok","data":{"publicStatus":true}}

//	REQUEST, v1.2.13:setPassword, {"password":"integration","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"g.ZlouqmXZVtoTu2pf$integration-test-1"}
//	RESPONSE, setPassword, {"code":0,"message":"ok","data":null}

//	REQUEST, v1.2.13:isPasswordProtected, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"g.ZlouqmXZVtoTu2pf$integration-test-1"}
//	RESPONSE, isPasswordProtected, {"code":0,"message":"ok","data":{"isPasswordProtected":true}}

//	[2018-11-17 19:02:14.963] [INFO] API - REQUEST, v1.2.13:createGroupPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.ZlouqmXZVtoTu2pf","padName":"integration-test-2","text":"Initial text"}
//	[2018-11-17 19:02:14.964] [INFO] API - RESPONSE, createGroupPad, {"code":0,"message":"ok","data":{"padID":"g.ZlouqmXZVtoTu2pf$integration-test-2"}}

//	[2018-11-17 19:02:14.968] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"g.ZlouqmXZVtoTu2pf$integration-test-2"}
//	[2018-11-17 19:02:14.971] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"Initial text\n"}}

//	[2018-11-17 19:02:14.974] [INFO] API - REQUEST, v1.2.13:listPads, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.ZlouqmXZVtoTu2pf"}
//	[2018-11-17 19:02:14.975] [INFO] API - RESPONSE, listPads, {"code":0,"message":"ok","data":{"padIDs":["g.ZlouqmXZVtoTu2pf$integration-test-1","g.ZlouqmXZVtoTu2pf$integration-test-2"]}}

//	[2018-11-17 19:02:14.980] [INFO] API - REQUEST, v1.2.13:deleteGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.ZlouqmXZVtoTu2pf"}
//	[2018-11-17 19:02:14.985] [INFO] API - RESPONSE, deleteGroup, {"code":0,"message":"ok","data":null}
//
//	
	@Test
	public void create_group_pads_and_list_them() throws Exception {
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.ZlouqmXZVtoTu2pf\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.ZlouqmXZVtoTu2pf&padName=integration-test-1")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.ZlouqmXZVtoTu2pf$integration-test-1\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPublicStatus")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.ZlouqmXZVtoTu2pf%24integration-test-1&publicStatus=true")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPublicStatus")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.ZlouqmXZVtoTu2pf$integration-test-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPassword")
				.withBody(new StringBody(
						"password=integration&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.ZlouqmXZVtoTu2pf%24integration-test-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/isPasswordProtected")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.ZlouqmXZVtoTu2pf$integration-test-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.ZlouqmXZVtoTu2pf&padName=integration-test-2&text=Initial+text")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.ZlouqmXZVtoTu2pf$integration-test-2\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.ZlouqmXZVtoTu2pf$integration-test-2")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPads").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.ZlouqmXZVtoTu2pf")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.ZlouqmXZVtoTu2pf$integration-test-1\",\"g.ZlouqmXZVtoTu2pf$integration-test-2\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.ZlouqmXZVtoTu2pf")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();
		String groupId = (String) response.get("groupID");
		String padName1 = "integration-test-1";
		String padName2 = "integration-test-2";
		try {
			Map padResponse = client.createGroupPad(groupId, padName1);
			assertTrue(padResponse.containsKey("padID"));
			String padId1 = (String) padResponse.get("padID");

			client.setPublicStatus(padId1, true);
			boolean publicStatus = (boolean) client.getPublicStatus(padId1).get("publicStatus");
			assertTrue(publicStatus);

			client.setPassword(padId1, "integration");
			boolean passwordProtected = (boolean) client.isPasswordProtected(padId1).get("isPasswordProtected");
			assertTrue(passwordProtected);

			padResponse = client.createGroupPad(groupId, padName2, "Initial text");
			assertTrue(padResponse.containsKey("padID"));

			String padId = (String) padResponse.get("padID");
			String initialText = (String) client.getText(padId).get("text");
			assertEquals("Initial text\n", initialText);

			Map padListResponse = client.listPads(groupId);

			assertTrue(padListResponse.containsKey("padIDs"));
			List padIds = (List) padListResponse.get("padIDs");

			assertEquals(2, padIds.size());
		} finally {
			client.deleteGroup(groupId);
		}
	}

//	[2018-11-17 19:29:18.434] [INFO] API - REQUEST, v1.2.13:createAuthor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	[2018-11-17 19:29:18.434] [INFO] API - RESPONSE, createAuthor, {"code":0,"message":"ok","data":{"authorID":"a.E91SuQv71kDEGF6w"}}
//	[2018-11-17 19:29:18.444] [INFO] API - REQUEST, v1.2.13:createAuthor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author"}
//	[2018-11-17 19:29:18.445] [INFO] API - RESPONSE, createAuthor, {"code":0,"message":"ok","data":{"authorID":"a.NeaHotX76EmfkQ7s"}}
//	[2018-11-17 19:29:18.447] [INFO] API - REQUEST, v1.2.13:getAuthorName, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.NeaHotX76EmfkQ7s"}
//	[2018-11-17 19:29:18.447] [INFO] API - RESPONSE, getAuthorName, {"code":0,"message":"ok","data":"integration-author"}

	@Test
	public void create_author() throws Exception {

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.E91SuQv71kDEGF6w\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.NeaHotX76EmfkQ7s\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.NeaHotX76EmfkQ7s")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());

		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

//	[2018-11-17 19:36:06.082] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author-1","authorMapper":"username"}
//	[2018-11-17 19:36:06.084] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.WJSHARpCUfeix0cH"}}
//	[2018-11-17 19:36:06.105] [INFO] API - REQUEST, v1.2.13:getAuthorName, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-17 19:36:06.105] [INFO] API - RESPONSE, getAuthorName, {"code":0,"message":"ok","data":"integration-author-1"}
//	[2018-11-17 19:36:06.110] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author-2","authorMapper":"username"}
//	[2018-11-17 19:36:06.111] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.WJSHARpCUfeix0cH"}}

//	[2018-11-17 19:36:06.115] [INFO] API - REQUEST, v1.2.13:getAuthorName, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-17 19:36:06.116] [INFO] API - RESPONSE, getAuthorName, {"code":0,"message":"ok","data":"integration-author-2"}

//	[2018-11-17 19:36:06.118] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorMapper":"username"}
//	[2018-11-17 19:36:06.118] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.WJSHARpCUfeix0cH"}}
//	[2018-11-17 19:36:06.120] [INFO] API - REQUEST, v1.2.13:getAuthorName, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-17 19:36:06.120] [INFO] API - RESPONSE, getAuthorName, {"code":0,"message":"ok","data":"integration-author-2"}

	@Test
	public void create_author_with_author_mapper() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.NeaHotX76EmfkQ7s\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.NeaHotX76EmfkQ7s")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));

		String authorMapper = "username";

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());

		String firstAuthorName = client.getAuthorName(firstAuthorId);
		mockServer.clear(
				HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.NeaHotX76EmfkQ7s")));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=username")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.NeaHotX76EmfkQ7s\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.NeaHotX76EmfkQ7s")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);

		String secondAuthorName = client.getAuthorName(secondAuthorId);

		assertNotEquals(firstAuthorName, secondAuthorName);

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorMapper=username")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.NeaHotX76EmfkQ7s\"}}"));
		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);

		assertEquals(secondAuthorName, thirdAuthorName);
	}

//	REQUEST, v1.2.13:createGroupIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupMapper":"groupname"}
//	[2018-11-18 10:17:59.586] [INFO] API - RESPONSE, createGroupIfNotExistsFor, {"code":0,"message":"ok","data":{"groupID":"g.OwkDpT5kTSJAqMZM"}}

//	[2018-11-18 10:17:59.609] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author-1","authorMapper":"username"}
//	[2018-11-18 10:17:59.610] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.WJSHARpCUfeix0cH"}}

//	[2018-11-18 10:17:59.615] [INFO] API - REQUEST, v1.2.13:createSession, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.OwkDpT5kTSJAqMZM","validUntil":"1542561479","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-18 10:17:59.617] [INFO] API - RESPONSE, createSession, {"code":0,"message":"ok","data":{"sessionID":"s.0d82930e2a8a52397dd1ee9b756aefb2"}}

//	[2018-11-18 10:17:59.627] [INFO] API - REQUEST, v1.2.13:createSession, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.OwkDpT5kTSJAqMZM","validUntil":"1574068679","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-18 10:17:59.628] [INFO] API - RESPONSE, createSession, {"code":0,"message":"ok","data":{"sessionID":"s.e72532167d9280b1f0348328e83fdda4"}}

//	[2018-11-18 10:17:59.631] [INFO] API - REQUEST, v1.2.13:getSessionInfo, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","sessionID":"s.e72532167d9280b1f0348328e83fdda4"}
//	[2018-11-18 10:17:59.632] [INFO] API - RESPONSE, getSessionInfo, {"code":0,"message":"ok","data":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1574068679}}

//	[2018-11-18 10:17:59.636] [INFO] API - REQUEST, v1.2.13:listSessionsOfGroup, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","groupID":"g.OwkDpT5kTSJAqMZM"}
//	[2018-11-18 10:17:59.637] [INFO] API - RESPONSE, listSessionsOfGroup, {"code":0,"message":"ok","data":{"s.a9bf01a03fec318382fab218f29016cf":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1542561252},"s.0d82930e2a8a52397dd1ee9b756aefb2":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1542561479},"s.e72532167d9280b1f0348328e83fdda4":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1574068679}}}

//  [2018-11-18 10:17:59.640] [INFO] API - REQUEST, v1.2.13:listSessionsOfAuthor,{"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","authorID":"a.WJSHARpCUfeix0cH"}
//	[2018-11-18 10:17:59.641] [INFO] API - RESPONSE, listSessionsOfAuthor, {"code":0,"message":"ok","data":{"s.a9bf01a03fec318382fab218f29016cf":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1542561252},"s.0d82930e2a8a52397dd1ee9b756aefb2":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1542561479},"s.e72532167d9280b1f0348328e83fdda4":{"groupID":"g.OwkDpT5kTSJAqMZM","authorID":"a.WJSHARpCUfeix0cH","validUntil":1574068679}}}

	// [2018-11-18 10:17:59.643] [INFO] API - REQUEST, v1.2.13:deleteSession,
	// {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","sessionID":"s.0d82930e2a8a52397dd1ee9b756aefb2"}
//	[2018-11-18 10:17:59.644] [INFO] API - RESPONSE, deleteSession, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:17:59.649] [INFO] API - REQUEST, v1.2.13:deleteSession, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","sessionID":"s.e72532167d9280b1f0348328e83fdda4"}
//	[2018-11-18 10:17:59.651] [INFO] API - RESPONSE, deleteSession, {"code":0,"message":"ok","data":null}

	@Test
	public void create_and_delete_session() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.WJSHARpCUfeix0cH\"}}"));

		String authorMapper = "username";
		String groupMapper = "groupname";

		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String authorId = (String) authorResponse.get("authorID");

		int sessionDuration = 8;

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.OwkDpT5kTSJAqMZM&validUntil="
								+ ((new Date()).getTime() + (sessionDuration * 60L * 60L * 1000L)) / 1000L
								+ "&authorID=a.WJSHARpCUfeix0cH")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.0d82930e2a8a52397dd1ee9b756aefb2\"}}"));

		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");

		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.OwkDpT5kTSJAqMZM&validUntil="
								+ sessionValidUntil.getTime() / 1000L + "&authorID=a.WJSHARpCUfeix0cH")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.e72532167d9280b1f0348328e83fdda4\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSessionInfo")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.e72532167d9280b1f0348328e83fdda4")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":"
								+ sessionValidUntil.getTime() / 1000L + "}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.OwkDpT5kTSJAqMZM")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\": {\"s.a9bf01a03fec318382fab218f29016cf\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1542561252},\"s.0d82930e2a8a52397dd1ee9b756aefb2\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1542561479},\"s.e72532167d9280b1f0348328e83fdda4\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1574068679}}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=a.WJSHARpCUfeix0cH")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\": {\"s.a9bf01a03fec318382fab218f29016cf\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1542561252},\"s.0d82930e2a8a52397dd1ee9b756aefb2\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1542561479},\"s.e72532167d9280b1f0348328e83fdda4\":{\"groupID\":\"g.OwkDpT5kTSJAqMZM\",\"authorID\":\"a.WJSHARpCUfeix0cH\",\"validUntil\":1574068679}}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.0d82930e2a8a52397dd1ee9b756aefb2")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\": null}"));

		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);

			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
			assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));

			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));

			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			client.deleteSession(firstSessionId);

			mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession"));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.e72532167d9280b1f0348328e83fdda4")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\": null}"));

			client.deleteSession(secondSessionId);
		}

	}

//	[2018-11-18 10:53:53.228] [INFO] API - REQUEST, v1.2.13:createPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.238] [INFO] API - RESPONSE, createPad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.261] [INFO] API - REQUEST, v1.2.13:setText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad","text":"gå å gjør et ærend"}
//	[2018-11-18 10:53:53.263] [INFO] API - RESPONSE, setText, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.269] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.271] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"gå å gjør et ærend\n"}}
//	[2018-11-18 10:53:53.276] [INFO] API - REQUEST, v1.2.13:setHTML, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad","html":"<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>"}
//	[2018-11-18 10:53:53.299] [INFO] API - RESPONSE, setHTML, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.302] [INFO] API - REQUEST, v1.2.13:getHTML, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.308] [INFO] API - RESPONSE, getHTML, {"code":0,"message":"ok","data":{"html":"<!DOCTYPE HTML><html><body>g&#229; og gj&#248;re et &#230;rend igjen<br><br></body></html>"}}
//	[2018-11-18 10:53:53.311] [INFO] API - REQUEST, v1.2.13:getHTML, {"rev":"2","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.314] [INFO] API - RESPONSE, getHTML, {"code":0,"message":"ok","data":{"html":"<!DOCTYPE HTML><html><body><br></body></html>"}}
//	[2018-11-18 10:53:53.317] [INFO] API - REQUEST, v1.2.13:getText, {"rev":"2","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.318] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"\n"}}
//	[2018-11-18 10:53:53.320] [INFO] API - REQUEST, v1.2.13:getRevisionsCount, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.320] [INFO] API - RESPONSE, getRevisionsCount, {"code":0,"message":"ok","data":{"revisions":3}}
//	[2018-11-18 10:53:53.322] [INFO] API - REQUEST, v1.2.13:getRevisionChangeset, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.323] [INFO] API - RESPONSE, getRevisionChangeset, {"code":0,"message":"ok","data":"Z:1>r|1+r$gå og gjøre et ærend igjen\n"}
//	[2018-11-18 10:53:53.325] [INFO] API - REQUEST, v1.2.13:getRevisionChangeset, {"rev":"2","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.325] [INFO] API - RESPONSE, getRevisionChangeset, {"code":0,"message":"ok","data":"Z:j<i|1-j|1+1$\n"}
//	[2018-11-18 10:53:53.328] [INFO] API - REQUEST, v1.2.13:createDiffHTML, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad","startRev":"1","endRev":"2"}
//	[2018-11-18 10:53:53.333] [INFO] API - RESPONSE, createDiffHTML, {"code":0,"message":"ok","data":{"html":"<style>\n.removed {text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\n</style><span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span><br><br>","authors":[""]}}
//	[2018-11-18 10:53:53.337] [INFO] API - REQUEST, v1.2.13:appendText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad","text":"lagt til nå"}
//	[2018-11-18 10:53:53.337] [INFO] API - RESPONSE, appendText, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.343] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.344] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"gå og gjøre et ærend igjen\nlagt til nå\n"}}
//	[2018-11-18 10:53:53.348] [INFO] API - REQUEST, v1.2.13:getAttributePool, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.348] [INFO] API - RESPONSE, getAttributePool, {"code":0,"message":"ok","data":{"pool":{"numToAttrib":{"0":["author",""],"1":["removed","true"]},"attribToNum":{"author,":0,"removed,true":1},"nextNum":2}}}
//	[2018-11-18 10:53:53.351] [INFO] API - REQUEST, v1.2.13:saveRevision, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.352] [INFO] API - RESPONSE, saveRevision, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.355] [INFO] API - REQUEST, v1.2.13:saveRevision, {"rev":"2","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.355] [INFO] API - RESPONSE, saveRevision, {"code":0,"message":"ok","data":null}
//	[2018-11-18 10:53:53.357] [INFO] API - REQUEST, v1.2.13:getSavedRevisionsCount, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.357] [INFO] API - RESPONSE, getSavedRevisionsCount, {"code":0,"message":"ok","data":{"savedRevisions":2}}
//	[2018-11-18 10:53:53.359] [INFO] API - REQUEST, v1.2.13:listSavedRevisions, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.360] [INFO] API - RESPONSE, listSavedRevisions, {"code":0,"message":"ok","data":{"savedRevisions":[2,4]}}
//	[2018-11-18 10:53:53.364] [INFO] API - REQUEST, v1.2.13:padUsersCount, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.364] [INFO] API - RESPONSE, padUsersCount, {"code":0,"message":"ok","data":{"padUsersCount":0}}
//	[2018-11-18 10:53:53.366] [INFO] API - REQUEST, v1.2.13:padUsers, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.366] [INFO] API - RESPONSE, padUsers, {"code":0,"message":"ok","data":{"padUsers":[]}}
//	[2018-11-18 10:53:53.368] [INFO] API - REQUEST, v1.2.13:getReadOnlyID, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.369] [INFO] API - RESPONSE, getReadOnlyID, {"code":0,"message":"ok","data":{"readOnlyID":"r.0c5f847d4dbf904e48743ff409a731c6"}}
//	[2018-11-18 10:53:53.373] [INFO] API - REQUEST, v1.2.13:getPadID, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","roID":"r.0c5f847d4dbf904e48743ff409a731c6"}
//	[2018-11-18 10:53:53.375] [INFO] API - RESPONSE, getPadID, {"code":0,"message":"ok","data":{"padID":"integration-test-pad"}}
//	[2018-11-18 10:53:53.377] [INFO] API - REQUEST, v1.2.13:listAuthorsOfPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.378] [INFO] API - RESPONSE, listAuthorsOfPad, {"code":0,"message":"ok","data":{"authorIDs":[]}}
//	[2018-11-18 10:53:53.381] [INFO] API - REQUEST, v1.2.13:getLastEdited, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.382] [INFO] API - RESPONSE, getLastEdited, {"code":0,"message":"ok","data":{"lastEdited":1542534833337}}
//	[2018-11-18 10:53:53.394] [INFO] API - REQUEST, v1.2.13:sendClientsMessage, {"msg":"test message","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.395] [INFO] API - RESPONSE, sendClientsMessage, {"code":0,"message":"ok","data":{}}
//	[2018-11-18 10:53:53.402] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 10:53:53.404] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}

	@Test
	public void create_pad_set_and_get_content() throws UnsupportedEncodingException {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=g%C3%A5+%C3%A5+gj%C3%B8r+et+%C3%A6rend")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\""
								+ new String("gå å gjør et ærend".getBytes(), "ISO-8859-1") + "\\n\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setHTML").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&html=%3C%21DOCTYPE+HTML%3E%3Chtml%3E%3Cbody%3E%3Cp%3Eg%C3%A5+og+gj%C3%B8re+et+%C3%A6rend+igjen%3C%2Fp%3E%3C%2Fbody%3E%3C%2Fhtml%3E")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body>g&#229; og gj&#248;re et &#230;rend igjen<br><br></body></html>\"}}"));

		String padID = "integration-test-pad";
		client.createPad(padID);
		try {
			client.setText(padID, "gå å gjør et ærend");
			String text = (String) client.getText(padID).get("text");
			assertEquals("gå å gjør et ærend\n", text);

			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
			String html = (String) client.getHTML(padID).get("html");
			assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><br></body></html>\"}}"));

			html = (String) client.getHTML(padID, 2).get("html");
			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"\\n\"}}"));

			text = (String) client.getText(padID, 2).get("text");
			assertEquals("\n", text);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionsCount")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"revisions\":3}}"));

			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
			assertEquals(3L, revisionCount);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\""
									+ new String("Z:1>r|1+r$gå og gjøre et ærend igjen".getBytes(), "ISO-8859-1")
									+ "\\n\"}"));

			String revisionChangeset = client.getRevisionChangeset(padID);
			assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\""
									+ new String("Z:j<i|1-j|1+1$".getBytes(), "ISO-8859-1") + "\\n\"}"));

			revisionChangeset = client.getRevisionChangeset(padID, 2);
			assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createDiffHTML")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&startRev=1&endRev=2")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<style>\\n.removed {text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\\n</style><span class=\\\"removed\\\">g&#229; &#229; gj&#248;r et &#230;rend</span><br><br>\",\"authors\":[\"\"]}}"));

			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
			assertTrue(diffHTML,
					diffHTML.contains("<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendText")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text="
									+ "lagt+til+n%C3%A5")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.appendText(padID, "lagt til nå");

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\""
									+ new String("gå og gjøre et ærend igjen\\nlagt til nå".getBytes(), "ISO-8859-1")
									+ "\\n\"}}"));

			text = (String) client.getText(padID).get("text");
			assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAttributePool")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"\"],\"1\":[\"removed\",\"true\"]},\"attribToNum\":{\"author,\":0,\"removed,true\":1},\"nextNum\":2}}}"));

			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
			assertTrue(attributePool.containsKey("attribToNum"));
			assertTrue(attributePool.containsKey("nextNum"));
			assertTrue(attributePool.containsKey("numToAttrib"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.saveRevision(padID);

			mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision"));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision")
					.withBody(new StringBody(
							"rev=2&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.saveRevision(padID, 2);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSavedRevisionsCount")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":2}}"));

			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
			assertEquals(2L, savedRevisionCount);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSavedRevisions")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":[2,4]}}"));

			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
			assertEquals(2, savedRevisions.size());
			assertEquals(2L, savedRevisions.get(0));
			assertEquals(4L, savedRevisions.get(1));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsersCount")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsersCount\":0}}"));

			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
			assertEquals(0, padUsersCount);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsers")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsers\":[]}}"));

			List padUsers = (List) client.padUsers(padID).get("padUsers");
			assertEquals(0, padUsers.size());

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getReadOnlyID")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"readOnlyID\":\"r.0c5f847d4dbf904e48743ff409a731c6\"}}"));

			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPadID")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&roID=r.0c5f847d4dbf904e48743ff409a731c6")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad\"}}"));

			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
			assertEquals(padID, padIdFromROId);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAuthorsOfPad")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorIDs\":[]}}"));

			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
			assertEquals(0, authorsOfPad.size());

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getLastEdited")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"lastEdited\":1542534833337}}"));

			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
			Calendar lastEdited = Calendar.getInstance();
			lastEdited.setTimeInMillis(lastEditedTimeStamp);
			Calendar now = Calendar.getInstance();
			assertTrue(lastEdited.before(now));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/sendClientsMessage")
					.withBody(new StringBody(
							"msg=test+message&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{}}"));

			client.sendClientsMessage(padID, "test message");
		} finally {

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.deletePad(padID);
		}
	}

//	[2018-11-18 11:47:21.584] [INFO] API - REQUEST, v1.2.13:createPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad","text":"should be kept"}
//	[2018-11-18 11:47:21.591] [INFO] API - RESPONSE, createPad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 11:47:21.609] [INFO] API - REQUEST, v1.2.13:copyPad, {"sourceID":"integration-test-pad","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","force":"false","destinationID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.621] [INFO] API - RESPONSE, copyPad, {"code":0,"message":"ok","data":{"padID":"integration-test-pad-copy"}}
//	[2018-11-18 11:47:21.626] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.628] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"should be kept\n"}}
//	[2018-11-18 11:47:21.631] [INFO] API - REQUEST, v1.2.13:copyPad, {"sourceID":"integration-test-pad","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","force":"false","destinationID":"integration-move-pad-move"}
//	[2018-11-18 11:47:21.641] [INFO] API - RESPONSE, copyPad, {"code":0,"message":"ok","data":{"padID":"integration-move-pad-move"}}
//	[2018-11-18 11:47:21.644] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-move-pad-move"}
//	[2018-11-18 11:47:21.644] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"should be kept\n"}}
//	[2018-11-18 11:47:21.647] [INFO] API - REQUEST, v1.2.13:setText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-move-pad-move","text":"should be changed"}
//	[2018-11-18 11:47:21.648] [INFO] API - RESPONSE, setText, {"code":0,"message":"ok","data":null}
//	[2018-11-18 11:47:21.652] [INFO] API - REQUEST, v1.2.13:copyPad, {"sourceID":"integration-move-pad-move","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","force":"true","destinationID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.665] [INFO] API - RESPONSE, copyPad, {"code":0,"message":"ok","data":{"padID":"integration-test-pad-copy"}}
//	[2018-11-18 11:47:21.668] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.669] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"should be changed\n"}}
//	[2018-11-18 11:47:21.671] [INFO] API - REQUEST, v1.2.13:movePad, {"sourceID":"integration-move-pad-move","apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","force":"true","destinationID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.683] [INFO] API - RESPONSE, movePad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 11:47:21.687] [INFO] API - REQUEST, v1.2.13:getText, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.688] [INFO] API - RESPONSE, getText, {"code":0,"message":"ok","data":{"text":"should be changed\n"}}
//	[2018-11-18 11:47:21.690] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-copy"}
//	[2018-11-18 11:47:21.691] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 11:47:21.693] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad"}
//	[2018-11-18 11:47:21.694] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}

	@Test
	public void create_pad_move_and_copy() throws Exception {
		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-move-pad-move";
		String keep = "should be kept";
		String change = "should be changed";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=should+be+kept")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.createPad(padID, keep);

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		client.copyPad(padID, copyPadId);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		// Impossible to avoid mutation. If internal call of copy pad is deleted, mocked
		// server can't be changed.
		String copyPadText = (String) client.getText(copyPadId).get("text");

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"));
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-move-pad-move")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));

		client.movePad(padID, movePadId);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		String movePadText = (String) client.getText(movePadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move&text=should+be+changed")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.setText(movePadId, change);

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"));
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));
		client.copyPad(movePadId, copyPadId, true);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));
		String copyPadTextForce = (String) client.getText(copyPadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad").withBody(new StringBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.movePad(movePadId, copyPadId, true);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-cppy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));
		String movePadTextForce = (String) client.getText(copyPadId).get("text");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(copyPadId);

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"));
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(padID);

		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);

		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);
	}

//	[2018-11-18 12:21:02.439] [INFO] API - REQUEST, v1.2.13:createPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:21:02.447] [INFO] API - RESPONSE, createPad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:21:02.465] [INFO] API - REQUEST, v1.2.13:createPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-2"}
//	[2018-11-18 12:21:02.466] [INFO] API - RESPONSE, createPad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:21:02.570] [INFO] API - REQUEST, v1.2.13:listAllPads, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"}
//	[2018-11-18 12:21:02.571] [INFO] API - RESPONSE, listAllPads, {"code":0,"message":"ok","data":{"padIDs":["g.ZlouqmXZVtoTu2pf$integration-test-1","g.ZlouqmXZVtoTu2pf$integration-test-2","g.eHzjiAOTZ1zeEqxN$integration-test-1","g.eHzjiAOTZ1zeEqxN$integration-test-2","iBsWYxqdkc","integration-move-pad-move","integration-test-pad","integration-test-pad-1","integration-test-pad-2","integration-test-pad-copy"]}}
//	[2018-11-18 12:21:02.576] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:21:02.579] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:21:02.582] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-2"}
//	[2018-11-18 12:21:02.582] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}

	@Test
	public void create_pads_and_list_them() throws InterruptedException {
		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.createPad(pad1);

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad"));
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.createPad(pad2);
		Thread.sleep(100);

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.ZlouqmXZVtoTu2pf$integration-test-1\",\"g.ZlouqmXZVtoTu2pf$integration-test-2\",\"g.eHzjiAOTZ1zeEqxN$integration-test-1\",\"g.eHzjiAOTZ1zeEqxN$integration-test-2\",\"iBsWYxqdkc\",\"integration-move-pad-move\",\"integration-test-pad\",\"integration-test-pad-1\",\"integration-test-pad-2\",\"integration-test-pad-copy\"]}}"));
		List padIDs = (List) client.listAllPads().get("padIDs");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(pad1);

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"));
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
		assertTrue(padIDs.contains(pad1));
		assertTrue(padIDs.contains(pad2));
	}

//	[2018-11-18 12:28:54.154] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author-1","authorMapper":"user1"}
//	[2018-11-18 12:28:54.156] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.SA3FfS0IfBIUTL6F"}}
//	[2018-11-18 12:28:54.176] [INFO] API - REQUEST, v1.2.13:createAuthorIfNotExistsFor, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","name":"integration-author-2","authorMapper":"user2"}
//	[2018-11-18 12:28:54.177] [INFO] API - RESPONSE, createAuthorIfNotExistsFor, {"code":0,"message":"ok","data":{"authorID":"a.s3aQp30mUCNoUotd"}}
//	[2018-11-18 12:28:54.179] [INFO] API - REQUEST, v1.2.13:createPad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:28:54.184] [INFO] API - RESPONSE, createPad, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:28:54.187] [INFO] API - REQUEST, v1.2.13:appendChatMessage, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1","text":"hi from user1","authorID":"a.SA3FfS0IfBIUTL6F"}
//	[2018-11-18 12:28:54.190] [INFO] API - RESPONSE, appendChatMessage, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:28:54.193] [INFO] API - REQUEST, v1.2.13:appendChatMessage, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1","text":"hi from user2","time":"1542540534","authorID":"a.s3aQp30mUCNoUotd"}
//	[2018-11-18 12:28:54.194] [INFO] API - RESPONSE, appendChatMessage, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:28:54.199] [INFO] API - REQUEST, v1.2.13:appendChatMessage, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1","text":"gå å gjør et ærend","time":"1542540534","authorID":"a.SA3FfS0IfBIUTL6F"}
//	[2018-11-18 12:28:54.199] [INFO] API - RESPONSE, appendChatMessage, {"code":0,"message":"ok","data":null}
//	[2018-11-18 12:28:54.203] [INFO] API - REQUEST, v1.2.13:getChatHead, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:28:54.204] [INFO] API - RESPONSE, getChatHead, {"code":0,"message":"ok","data":{"chatHead":2}}
//	[2018-11-18 12:28:54.208] [INFO] API - REQUEST, v1.2.13:getChatHistory, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:28:54.210] [INFO] API - RESPONSE, getChatHistory, {"code":0,"message":"ok","data":{"messages":[{"text":"hi from user1","userId":"a.SA3FfS0IfBIUTL6F","time":1542540534187,"userName":"integration-author-1"},{"text":"hi from user2","userId":"a.s3aQp30mUCNoUotd","time":1542540534,"userName":"integration-author-2"},{"text":"gå å gjør et ærend","userId":"a.SA3FfS0IfBIUTL6F","time":1542540534,"userName":"integration-author-1"}]}}
//	[2018-11-18 12:28:54.215] [INFO] API - REQUEST, v1.2.13:getChatHistory, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","start":"0","padID":"integration-test-pad-1","end":"1"}
//	[2018-11-18 12:28:54.216] [INFO] API - RESPONSE, getChatHistory, {"code":0,"message":"ok","data":{"messages":[{"text":"hi from user1","userId":"a.SA3FfS0IfBIUTL6F","time":1542540534187,"userName":"integration-author-1"},{"text":"hi from user2","userId":"a.s3aQp30mUCNoUotd","time":1542540534,"userName":"integration-author-2"}]}}
//	[2018-11-18 12:28:54.219] [INFO] API - REQUEST, v1.2.13:deletePad, {"apikey":"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58","padID":"integration-test-pad-1"}
//	[2018-11-18 12:28:54.222] [INFO] API - RESPONSE, deletePad, {"code":0,"message":"ok","data":null}

	@Test
	public void create_pad_and_chat_about_it() throws UnsupportedEncodingException {
		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=user1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.SA3FfS0IfBIUTL6F\"}}"));

		Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
		String author1Id = (String) response.get("authorID");

		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"));
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=user2")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.s3aQp30mUCNoUotd\"}}"));

		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
		String author2Id = (String) response.get("authorID");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.createPad(padID);
		try {

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user1&authorID=a.SA3FfS0IfBIUTL6F")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.SA3FfS0IfBIUTL6F\"}}"));

			Map message = client.appendChatMessage(padID, "hi from user1", author1Id);
			assertEquals("a.SA3FfS0IfBIUTL6F", message.get("authorID"));

			long timeMillisMsg1 = System.currentTimeMillis() / 1000L;

			mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage"));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user2"
									+ "&time=" + timeMillisMsg1 + "&authorID=a.s3aQp30mUCNoUotd")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.SA3FfS0IfBIUTL6F\"}}"));

			message = client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
			assertEquals("a.SA3FfS0IfBIUTL6F", message.get("authorID"));

			long timeMillisMsg2 = System.currentTimeMillis() / 1000L;
			
			mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage"));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"
									+ "&text=g%C3%A5+%C3%A5+gj%C3%B8r+et+%C3%A6rend&time=" + timeMillisMsg2
									+ "&authorID=a.SA3FfS0IfBIUTL6F")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.SA3FfS0IfBIUTL6F\"}}"));

			message = client.appendChatMessage(padID, "gå å gjør et ærend", author1Id,
					timeMillisMsg2);
			assertEquals("a.SA3FfS0IfBIUTL6F", message.get("authorID"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));

			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.SA3FfS0IfBIUTL6F\",\"time\":"
									+ timeMillisMsg1 + ",\"userName\":\"integration-author-1\"},"
									+ "{\"text\":\"hi from user2\",\"userId\":\"a.s3aQp30mUCNoUotd\",\"time\":"
									+ timeMillisMsg2 + ",\"userName\":\"integration-author-2\"}," + "{\"text\":\""
									+ new String("gå å gjør et ærend".getBytes(), "ISO-8859-1")
									+ "\",\"userId\":\"a.SA3FfS0IfBIUTL6F\",\"time\":" + timeMillisMsg2
									+ ",\"userName\":\"integration-author-1\"}]}}"));
			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());
			assertEquals("gå å gjør et ærend", ((Map) chatHistory.get(2)).get("text"));

			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&start=0&padID=integration-test-pad-1&end=1")))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.SA3FfS0IfBIUTL6F\",\"time\":"
									+ timeMillisMsg1 + ",\"userName\":\"integration-author-1\"},"
									+ "{\"text\":\"hi from user2\",\"userId\":\"a.s3aQp30mUCNoUotd\",\"time\":"
									+ timeMillisMsg2 + ",\"userName\":\"integration-author-2\"}]}}"));
			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
		} finally {

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
					.withBody(new StringBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deletePad(padID);
		}

	}

	// Stress test
	@Test
	public void createManySessions() {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=Stress+testing+the+application")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String padID = "integration-test-pad";
		client.createPad(padID);

		for (int i = 0; i < 100; i++) {
			client.setText(padID, "Stress testing the application");
		}
	}

	@Test
	public void createClientWithArguments() {

		EPLiteClient newClient = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58", "1.2.13", "UTF-8");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.MAPOoRpxOr5vYwkY\"}}"));

		Map response = newClient.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));
	}

	@Test
	public void listPadsOfAuthorTest() {

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.E91SuQv71kDEGF6w\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPadsOfAuthor")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.E91SuQv71kDEGF6w")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[]}}"));

		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());

		List pads = (List) client.listPadsOfAuthor(authorId).get("padIDs");
		assertEquals(0, pads.size());
	}

	@Test
	public void testSecureConnection() {

		assertEquals(false, client.isSecure());
		assertFalse(client.isSecure());
	}
}
