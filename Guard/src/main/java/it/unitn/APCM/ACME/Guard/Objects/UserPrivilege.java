package it.unitn.APCM.ACME.Guard.Objects;

/**
 * The type User privilege.
 */
public class UserPrivilege {
	/**
	 * The Admin set to 1 if the user is admin, 0 otherwise.
	 */
	private final int admin;
	/**
	 * The Groups that the user is part of.
	 */
	private final String groups;

	/**
	 * Instantiates a new User privilege.
	 *
	 * @param admin  the admin
	 * @param groups the groups
	 */
	public UserPrivilege(int admin, String groups){
        this.admin = admin;
        this.groups = groups;
    }

	/**
	 * Get admin value
	 *
	 * @return the int representing the admin privilege value
	 */
	public int getAdmin(){ return this.admin; }

	/**
	 * Get groups string.
	 *
	 * @return the string representing the group privilege value
	 */
	public String getGroups(){ return this.groups; }
}
