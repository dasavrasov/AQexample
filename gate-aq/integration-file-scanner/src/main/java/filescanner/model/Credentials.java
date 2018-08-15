package filescanner.model;

import lombok.Getter;

@Getter
public class Credentials {
	private Long id;
	private String loginname;
	private String pass;
	
	public Credentials(Long id, String username, String password) {
		super();
		this.id = id;
		this.loginname = username;
		this.pass = password;
	}
	
}
