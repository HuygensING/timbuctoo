/*
 * #%L
 * Timbuctoo configuration war example
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
var config = {
	/*
	 * serverUri should be pointing to the public url of the application, when a
	 * proxy is used this should point to the proxy address.
	 */
	serverUri : "localhost:8080/timbuctoo",
	/*
	 * securtityUri should point to url where the user can login. 
	 */
	securityUri : "http://localhost:8080/timbuctoo/static/example_vre/example_login.html"
};
