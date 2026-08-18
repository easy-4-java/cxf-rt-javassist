/*
 * Copyright (c) 2018, hiwepy (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cxf.endpoint.jaxrs;


import java.util.Date;  
 
import jakarta.xml.bind.annotation.XmlRootElement;  
 
public class Customer {    
   private String id;    
   private String name;    
   private Date birthday;    
   public String getId() {    
       return id;    
   }    
   public void setId(String id) {    
       this.id = id;    
   }    
   public String getName() {    
       return name;    
   }    
   public void setName(String name) {    
       this.name = name;    
   }    
   public Date getBirthday() {    
       return birthday;    
   }    
   public void setBirthday(Date birthday) {    
       this.birthday = birthday;    
   }    
   /*@Override   
   public String toString() {   
       return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this);   
   }  */  
}   