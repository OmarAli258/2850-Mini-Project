{% extends "base.peb" %}

{% block content %}

<h1>Books</h1>

{% if books is empty %}
    <p>No books available.</p>
{% else %}
    <table border="1" cellpadding="5">
        <tr>
            <th>Title</th>
            <th>Author</th>
            <th>Available</th>
            <th>Total</th>
        </tr>

        {% for b in books %}
        <tr>
            <td>{{ b.title }}</td>
            <td>{{ b.author }}</td>
            <td>{{ b.available }}</td>
            <td>{{ b.total }}</td>
        </tr>
        {% endfor %}
    </table>
{% endif %}

{% endblock %}