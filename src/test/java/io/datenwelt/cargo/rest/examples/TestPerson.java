/*
 * Copyright 2016 job.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datenwelt.cargo.rest.examples;

import java.util.Objects;
import org.joda.time.LocalDate;

/**
 *
 * @author job
 */
public class TestPerson {
    
    private String firstname;
    private String lastname;
    private LocalDate birthday;

    public TestPerson() {
    }
    
    public TestPerson(String firstname, String lastname, LocalDate birthday) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
    }
    
    public static TestPerson sample() {
        TestPerson person = new TestPerson();
        person.setFirstname("Test");
        person.setLastname("Testmann");
        person.setBirthday(new LocalDate(1970, 10, 23));
        return person;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.firstname);
        hash = 97 * hash + Objects.hashCode(this.lastname);
        hash = 97 * hash + Objects.hashCode(this.birthday);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestPerson other = (TestPerson) obj;
        if (!Objects.equals(this.firstname, other.firstname)) {
            return false;
        }
        if (!Objects.equals(this.lastname, other.lastname)) {
            return false;
        }
        if (!Objects.equals(this.birthday, other.birthday)) {
            return false;
        }
        return true;
    }
    
}
