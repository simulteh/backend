def add(a, b):
    return a + b

def divide(a, b):
    if b == 0:
        raise ValueError("Деление на ноль!")
    return a / b
print(add(3, 5))
print(duvide(3, 5))