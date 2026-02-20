<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>{{ title }}</title>
</head>
<body>

<nav>
    <a href="/">Home</a> |
    <a href="/books">Books</a> |

    {% if session %}
        <span>Logged in as {{ session.username }}</span>
        <form action="/logout" method="post" style="display:inline;">
            <button type="submit">Logout</button>
        </form>
    {% else %}
        <a href="/login">Login</a>
    {% endif %}
</nav>

<hr>

{% block content %}
{% endblock %}

</body>
</html>