/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)JSEJBIFrameworkMBean.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package net.openesb.standalone.framework;

/**
 *  Management interface for Java SE JBI framework.
 *
 * @author Sun Microsystems, Inc.
 */
public interface JSEJBIFrameworkMBean 
{
    /** Queries the state of the JBI Framework.
     *  @return true if the JBI framework is loaded, false otherwise.
     */
    boolean isLoaded();
    
    /** Load the JBI framework with the specified environment.  When this method
     *  retuns, all public interfaces and system services have completely 
     *  initialized.  If a connector port is specified in the environment 
     *  properties, a remote JMX connector server is created.
     *  @throws Exception failed to load JBI framework
     */
    void load() throws Exception;
    
    /** Unloads the JBI framework.  When this method retuns, all 
     *  public interfaces, system services, and JMX connector (if configured)
     *  have been destroyed.
     *  @throws javax.jbi.JBIException failed to unload JBI framework
     */
    void unload() throws Exception;
}
