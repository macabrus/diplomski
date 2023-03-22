
class View:
    ...

ProblemView = View(
    model=Problem,
    id=('id',),
    insert_keys=keys(Problem)
)

RunView = View(
    model=EvolutionState,
    ref={ # we infer whether it is single or list from prop definition in model
        'problem': ProblemView
    },
    agg={
        'populations': Populations
    }
)

# select(db, RunView, (RunView.keys.id == 1) & (RunView.keys.problem < 3))
# insert(db, RunView, obj)
# update(db, RunView, obj, deep=True)
# delete(db)
view_schema = [ProblemView, ShortRunView]

# list_of_models = reduce_rows(cur, Model)