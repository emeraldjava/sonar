/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.bytecode.check;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.ast.SquidTestUtils;
import org.sonar.java.bytecode.BytecodeScanner;
import org.sonar.java.squid.JavaSquidConfiguration;
import org.sonar.squid.Squid;
import org.sonar.squid.api.CheckMessage;
import org.sonar.squid.api.SourceFile;

public class UnusedPrivateMethodCheckTest {

  private static Squid squid;

  @BeforeClass
  public static void setup() {
    squid = new Squid(new JavaSquidConfiguration());
    squid.register(JavaAstScanner.class).scanDirectory(SquidTestUtils.getFile("/bytecode/unusedPrivateMethod/src"));
    squid.registerVisitor(UnusedPrivateMethodCheck.class);
    squid.register(BytecodeScanner.class).scanDirectory(SquidTestUtils.getFile("/bytecode/unusedPrivateMethod/bin"));
  }

  @Test
  public void testDetectUnusedPrivateMethod() {
    SourceFile file = (SourceFile) squid.search("UnusedPrivateMethod.java");

    assertThat(file.getCheckMessages().size(), is(1));
    CheckMessage message = file.getCheckMessages().iterator().next();
    assertThat(message.getLine(), is(33));
    assertThat(message.getDefaultMessage(), is("Private method 'unusedPrivateMethod(...)' is never used."));
  }
  
  @Test
  public void testDetectUnusedGenericPrivateMethod() {
    SourceFile file = (SourceFile) squid.search("UnusedGenericPrivateMethod.java");

    assertThat(file.getCheckMessages().size(), is(1));
    CheckMessage message = file.getCheckMessages().iterator().next();
    assertThat(message.getLine(), is(7));
  }

  @Test
  public void testDetectUnusedPrivateConstructor() {
    SourceFile file = (SourceFile) squid.search("UnusedPrivateConstructor.java");

    assertThat(file.getCheckMessages().size(), is(1));
    CheckMessage message = file.getCheckMessages().iterator().next();
    assertThat(message.getLine(), is(10));
    assertThat(message.getDefaultMessage(), is("Private method '<init>(...)' is never used."));
  }
}
