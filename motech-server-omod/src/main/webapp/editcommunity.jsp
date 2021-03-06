<%--

    MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT

    Copyright (c) 2010-11 The Trustees of Columbia University in the City of
    New York and Grameen Foundation USA.  All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

    3. Neither the name of Grameen Foundation USA, Columbia University, or
    their respective contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
    AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
    FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
    USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Register MoTeCH Communities" otherwise="/login.htm"
                 redirect="/module/motechmodule/editfacility.form"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<meta name="heading" content="Edit Community"/>
<%@ include file="localHeader.jsp" %>
<h2>Edit a Community</h2>

<div class="instructions">
    Edit community attributes and click submit to save.
</div>

<c:url value="/module/motechmodule/community/editcommunity.form" var="submitAction"/>
<form:form method="post" modelAttribute="community" action="${submitAction}">
    <form:errors cssClass="error"/>
    <fieldset>
        <legend>Edit</legend>
        <table>
            <tr>
                <td><form:label path="communityId">Community Id:</form:label></td>
                <td><form:input path="communityId" disabled="true"/></td>
                <td><form:hidden path="communityId"/></td>
            </tr>
            <tr>
                <td><form:label path="name">Name :</form:label></td>
                <td><form:input path="name"/></td>
                <td><form:errors path="name" cssClass="error"/></td>
            </tr>
            <tr>
                <td><form:label path="facilityId">Facility</form:label></td>
                <td>
                    <form:select path="facilityId">
                        <c:forEach items="${facilities}" var="facility">
                            <form:option
                                    value="${facility.facilityId}">${facility.location.name}</form:option>
                        </c:forEach>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Submit">
                </td>
            </tr>
        </table>

    </fieldset>
</form:form>


<%@ include file="/WEB-INF/template/footer.jsp" %>