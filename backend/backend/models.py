
from attr import define, field
import random

@define(kw_only=True)
class Problem:
    label: str
    descirption: str
    id: int = None
    color: str = field(factory=lambda: '#{:02x}{:02x}{:02x}'.format(*map(lambda x: random.randint(0, 255), range(3))))
    costs: dict[tuple[int, int], float] = field(factory=dict)
