package opticraft.util;

import com.google.gson.*;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Accounts {

	public File location = new File(System.getProperty("user.home"), "HyClient");
	public File file = new File(location, "saved_accounts.json");
	public Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static ArrayList<Account> list = new ArrayList<>();
	public static int selected = -1;

	public static MicrosoftAuthResult login() {
		if(selected==-1) return null;
		Account a = list.get(selected);

		return a.login();
	}

	public void save() {
		try {
			if(!file.exists()) file.createNewFile();

			JsonObject obj = new JsonObject();
			obj.addProperty("selected", selected);

			JsonArray arr = new JsonArray();
			for(Account a : list) {
				JsonObject o = new JsonObject();
				o.addProperty("ref_token", a.refToken);
				o.addProperty("acc_token", a.accToken);
				o.addProperty("nickname", a.nick);
				arr.add(o);
			}
			obj.add("list", arr);

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(obj));
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void loadAccounts() {
		try {
			if(file.exists()) {
				StringBuilder res = new StringBuilder();

				Scanner s = new Scanner(file);
				while(s.hasNextLine()) res.append(s.nextLine());
				s.close();

				JsonObject obj = gson.fromJson(res.toString(), JsonObject.class);
				for(JsonElement acc : obj.get("list").getAsJsonArray()) {
					JsonObject a = acc.getAsJsonObject();

					list.add(new Account(
							a.get("ref_token").getAsString(),
							a.get("acc_token").getAsString(),
							a.get("nickname").getAsString()
					));
				}
				selected = obj.get("selected").getAsInt();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static class Account {
		public String refToken;
		public String accToken;
		public String nick;
		public String type;
		public Account(String refToken, String accToken, String nick) {
			this.refToken = refToken;
			this.accToken = accToken;
			this.nick = nick;
		}

		public MicrosoftAuthResult login() {
			try {
				MicrosoftAuthenticator a = new MicrosoftAuthenticator();
				MicrosoftAuthResult res = a.loginWithRefreshToken(this.refToken);
				return res;
			} catch (MicrosoftAuthenticationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
