/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package org.mcsoxford.rss;

/**
 * Non-recoverable runtime exception.
 * 
 * @author Mr Horn
 */
public class RSSFault extends RuntimeException {

  /**
   * Unsupported serialization
   */
  private static final long serialVersionUID = 1L;

  public RSSFault(String message) {
    super(message);
  }

  public RSSFault(Throwable cause) {
    super(cause);
  }

  public RSSFault(String message, Throwable cause) {
    super(message, cause);
  }

}
