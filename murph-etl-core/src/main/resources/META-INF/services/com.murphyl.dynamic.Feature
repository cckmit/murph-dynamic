# job schema loaders
com.murphyl.etl.schema.loader.impl.FilesystemJobSchemaLoader

# job schema parsers
com.murphyl.etl.schema.parser.impl.XmlJobSchemaParser

# extractors
com.murphyl.etl.task.children.extractor.impl.RandomExtractor
com.murphyl.etl.task.children.extractor.impl.TextFileExtractor
com.murphyl.etl.task.children.extractor.impl.JdbcExtractor

# transforms
com.murphyl.etl.task.children.transformer.impl.LogicFilterTransformer

# loaders
com.murphyl.etl.task.children.loader.impl.ConsoleLoader
com.murphyl.etl.task.children.loader.impl.TextFileLoader
