

from attr import define


@define(kw_only=True)
class ShortPopulation:
    id: int
    label: str
