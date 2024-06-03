using ApiAndroid.Entities;
using Microsoft.EntityFrameworkCore;

namespace ApiAndroid.Data
{
    public class DatabaseContext : DbContext
    {
        public DatabaseContext(DbContextOptions<DatabaseContext> options) : base(options)
        {

        }

        public DbSet<Product> Products { get; set; }
        public DbSet<ProductPhoto> ProductPhotos { get; set; }
        public DbSet<Document> Documents { get; set; }
    }
}
