
class View:
    ...

ProblemView = View(
    namespace='prob',
    prefix='prob',
    insert_keys=keys(Problem)
)

ShortRunView = View(
    namespace='r',
    prefix='r',
    model=EvolutionState,
    ref={ # we infer whether it is single or list from prop definition in model
        'problem': ProblemView
    },
    agg={
        'populations': Populations
    }
)

view_schema = [ProblemView, ShortRunView]

# list_of_models = reduce_rows(cur, Model)