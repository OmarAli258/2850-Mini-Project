{% extends "base.peb" %}

{% block content %}

<h1>Login</h1>

{% if error %}
    <p style="color:red;">{{ error }}</p>
{% endif %}

<form action="/login" method="post">
    <div>
        <label>Username:</label>
        <input type="text" name="username" required>
    </div>

    <div>
        <label>Password:</label>
        <input type="password" name="password" required>
    </div>

    <div>
        <button type="submit">Login</button>
    </div>
</form>

{% endblock %}